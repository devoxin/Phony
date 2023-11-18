package pro.serux.telephony.io

import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class StreamWriter(private val stream: OutputStream) {
    private fun <T : Number> inOrder(order: ByteOrder, v: T, bytes: Int, put: ByteBuffer.(T) -> ByteBuffer): ByteArray {
        return ByteBuffer.allocate(bytes).order(order).put(v).array()
    }

    fun writeInt16LE(s: Int) = stream.write(inOrder(ByteOrder.LITTLE_ENDIAN, s.toShort(), bytes = 2, put = ByteBuffer::putShort))
    fun writeUInt32LE(i: Int) = stream.write(inOrder(ByteOrder.LITTLE_ENDIAN, i, bytes = 4, put = ByteBuffer::putInt))
    fun writeArray(byteArray: ByteArray) = stream.write(byteArray)

    fun finish() {
        stream.flush()
        stream.close()
    }
}
