package pro.serux.telephony.io

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class StreamWriter {

    private val buffer = ByteArrayOutputStream()
    private val stream = DataOutputStream(buffer)

    private fun shortAsLittleEndian(v: Short): ByteArray {
        val buffer = ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(v)

        return buffer.array()
    }

    private fun intAsLittleEndian(v: Int): ByteArray {
        val buffer = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(v)

        return buffer.array()
    }

    fun writeInt16LE(s: Int) = stream.write(shortAsLittleEndian(s.toShort()))
    fun writeUInt32LE(i: Int) = stream.write(intAsLittleEndian(i))
    fun writeArray(byteArray: ByteArray) = stream.write(byteArray)

    fun flush(): ByteArray {
        stream.flush()

        val output = buffer.toByteArray()
        stream.close()
        return output
    }

}
