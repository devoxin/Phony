package pro.serux.telephony.io

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

class StreamReader {
    private val bytes: ByteArray
    val length: Int get() = bytes.size

    constructor(inputStream: InputStream): this(inputStream.readAllBytes())

    constructor(byteArray: ByteArray) {
        this.bytes = byteArray
    }

    private fun read(offset: Int, len: Int): ByteArray {
        return bytes.copyOfRange(offset, offset + len)
    }

    operator fun get(index: Int) = readByte(index).toInt()

    fun readUTF(offset: Int, len: Int) = String(read(offset, len))

    fun readByte(index: Int) = bytes[index] and 0xFF.toByte()

    fun readUInt8(index: Int) = readByte(index).toInt()


    // 0, 2
    fun readInt16LE(index: Int) = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort(index)

    fun readUInt32BE(offset: Int) = ByteBuffer.wrap(read(offset, 4)).order(ByteOrder.BIG_ENDIAN).int
    fun readUInt32LE(offset: Int) = ByteBuffer.wrap(read(offset, 4)).order(ByteOrder.LITTLE_ENDIAN).int

    fun section(offset: Int, len: Short) =
        StreamReader(bytes.copyOfRange(offset, offset + len))
    fun section(offset: Int, len: Int) =
        StreamReader(bytes.copyOfRange(offset, offset + len))
    fun section(offset: Int) = StreamReader(read(offset, length - offset - 1))

    fun toByteArray() = bytes
    override fun toString() = String(bytes)

}
