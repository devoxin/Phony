package pro.serux.telephony.audio;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class StereoPcmAudioMixer {
  private final short[] mixBuffer;
  private final byte[] outputBuffer;
  private final ShortBuffer wrappedOutput;
  private final Multiplier multiplier = new Multiplier();
  boolean hasData = false;

  public StereoPcmAudioMixer(int sampleCount, boolean isBigEndian) {
    this.mixBuffer = new short[sampleCount * 2];
    this.outputBuffer = new byte[sampleCount * 4];
    this.wrappedOutput = ByteBuffer.wrap(outputBuffer)
            .order(isBigEndian ? BIG_ENDIAN : LITTLE_ENDIAN)
            .asShortBuffer();
  }

  public void reset() {
    Arrays.fill(mixBuffer, (short) 0);
    hasData = false;
  }

  public void add(byte[] data) {
    if (data != null) {
      ShortBuffer inputBuffer = ByteBuffer.wrap(data)
              .order(BIG_ENDIAN) // we receive big endian pcm samples
              .asShortBuffer();

      if (!hasData) {
        inputBuffer.get(mixBuffer);
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

    if (multiplier.requiresAdjusting()) {
      for (int i = 0; i < mixBuffer.length; i++) { // i = 10
        wrappedOutput.put(i, coerceIn(mixBuffer[i] * multiplier.adjustment, -32768, 32767));
      }

//      for (int i = 0; i < 10; i++) {
//        float gradientMultiplier = (currentMultiplier.value * i + previousMultiplier.value * (10 - i)) * 0.1f;
//        wrappedOutput.put(i, (short) coerceIn(gradientMultiplier * mixBuffer[i], -32767, 32767));
//      }
    } else {
      for (int i = 0; i < mixBuffer.length; i++) {
        wrappedOutput.put(i, coerceIn(mixBuffer[i], -32768, 32767));
      }
    }

    reset();
    return outputBuffer;
  }

  private void updateMultiplier() {
    short peak = 0;

    if (hasData) {
      for (short value : mixBuffer) {
        peak = (short) Math.max(peak, Math.abs(value));
      }
    }

    multiplier.setAdjustmentForPeak(peak);
  }

  private short coerceIn(float value, int min, int max) {
    if (value < min) {
      return (short) min;
    } else {
      return (short) Math.min(value, max);
    }
  }

  private static class Multiplier {
    private float adjustment = 1.0f;

    public void setAdjustmentForPeak(short peak) {
      this.adjustment = (float) 32767 / peak;
    }

    public boolean requiresAdjusting() {
      return adjustment != 1.0f;
    }
  }
}
