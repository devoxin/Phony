package pro.serux.telephony.misc

import java.nio.ByteBuffer
import java.nio.ByteOrder

class StereoPcmMixer(sampleCount: Int) {
    private val outArray = ByteArray(sampleCount * 4)

    private val mixBuffer = IntArray(sampleCount * 2)
    private val outBuffer = ByteBuffer.wrap(outArray)
        .order(ByteOrder.BIG_ENDIAN)
        .asShortBuffer()


    var empty = true
        private set

    fun add(data: ByteArray) {
        val shortBuffer = ByteBuffer.wrap(data)
            .order(ByteOrder.BIG_ENDIAN)
            .asShortBuffer()

        if (empty) {
            for (i in mixBuffer.indices) {
                mixBuffer[i] = shortBuffer.get(i).toInt()
            }

            empty = false
        } else {
            for (i in mixBuffer.indices) {
                mixBuffer[i] += shortBuffer.get(i).toInt()
            }
        }
    }

    fun flush(): ByteBuffer {
        for (a in mixBuffer.indices) {
            outBuffer.put(a, mixBuffer[a].toShort())
        }

        empty = true
        return ByteBuffer.wrap(outArray)
    }
}
