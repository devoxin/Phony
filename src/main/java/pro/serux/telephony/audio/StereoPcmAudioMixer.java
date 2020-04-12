package pro.serux.telephony.audio;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class StereoPcmAudioMixer {
  private final int[] mixBuffer;
  private final byte[] outputBuffer;
  private final ShortBuffer wrappedOutput;
  private final Multiplier previousMultiplier = new Multiplier();
  private final Multiplier currentMultiplier = new Multiplier();
  boolean hasData = false;

  public StereoPcmAudioMixer(int sampleCount, boolean isBigEndian) {
    this.mixBuffer = new int[sampleCount * 2];
    this.outputBuffer = new byte[sampleCount * 4];
    this.wrappedOutput = ByteBuffer.wrap(outputBuffer)
            .order(isBigEndian ? BIG_ENDIAN : LITTLE_ENDIAN)
            .asShortBuffer();
  }

  public void reset() {
    hasData = false;
  }

  public void add(byte[] data) {
    if (data != null) {
      ShortBuffer inputBuffer = ByteBuffer.wrap(data)
              .order(BIG_ENDIAN)// : LITTLE_ENDIAN)
              .asShortBuffer();

      if (!hasData) {
        for (int i = 0; i < mixBuffer.length; i++) {
          mixBuffer[i] = inputBuffer.get(i);
        }

        hasData = true;
      } else {
        for (int i = 0; i < mixBuffer.length; i++) {
          mixBuffer[i] += inputBuffer.get(i);
        }
      }
    }
  }

  public byte[] get() {
    updateMultiplier();

    if (!currentMultiplier.identity || !previousMultiplier.identity) {
      for (int i = 0; i < 10; i++) {
        float gradientMultiplier = (currentMultiplier.value * i + previousMultiplier.value * (10 - i)) * 0.1f;
        wrappedOutput.put(i, (short) coerceIn(gradientMultiplier * mixBuffer[i], -32767, 32767));
      }

      for (int i = 10; i < mixBuffer.length; i++) {
        wrappedOutput.put(i, (short) coerceIn(currentMultiplier.value * mixBuffer[i], -32767, 32767));
      }

      previousMultiplier.identity = currentMultiplier.identity;
      previousMultiplier.value = currentMultiplier.value;
    } else {
      for (int i = 0; i < mixBuffer.length; i++) {
        wrappedOutput.put(i, (short) coerceIn(mixBuffer[i], -32767, 32767));
      }
    }

    reset();
    return outputBuffer;
  }

  private void updateMultiplier() {
    int peak = 0;

    if (hasData) {
      for (int value : mixBuffer) {
        peak = Math.max(peak, Math.abs(value));
      }
    }

    if (peak > 32768) {
      currentMultiplier.identity = false;
      currentMultiplier.value = 32768.0f / peak;
    } else {
      currentMultiplier.identity = true;
      currentMultiplier.value = 1.0f;
    }
  }

  private float coerceIn(float value, float min, float max) {
    if (value < min) {
      return min;
    } else {
      return Math.min(value, max);
    }
  }

  private static class Multiplier {
    private boolean identity = true;
    private float value = 1.0f;
  }
}
