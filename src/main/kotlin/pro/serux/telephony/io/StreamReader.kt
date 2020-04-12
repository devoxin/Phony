package pro.serux.telephony.io

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

class StreamReader {
    private val fuckMyLife: ByteArray
    val length: Int get() = fuckMyLife.size

    constructor(inputStream: InputStream): this(inputStream.readAllBytes())

    constructor(byteArray: ByteArray) {
        this.fuckMyLife = byteArray
    }

    private fun read(offset: Int, len: Int): ByteArray {
        return fuckMyLife.copyOfRange(offset, offset + len)
    }

    operator fun get(index: Int) = readByte(index).toInt()

    fun readUTF(offset: Int, len: Int) = String(read(offset, len))

    fun readByte(index: Int) = fuckMyLife[index] and 0xFF.toByte()

    fun readUInt8(index: Int) = readByte(index).toInt()


    // 0, 2
    fun readInt16LE(index: Int) = ByteBuffer.wrap(fuckMyLife).order(ByteOrder.LITTLE_ENDIAN).getShort(index)

    fun readUInt32BE(offset: Int) = ByteBuffer.wrap(read(offset, 4)).order(ByteOrder.BIG_ENDIAN).int
    fun readUInt32LE(offset: Int) = ByteBuffer.wrap(read(offset, 4)).order(ByteOrder.LITTLE_ENDIAN).int

    fun section(offset: Int, len: Short) =
        StreamReader(fuckMyLife.copyOfRange(offset, offset + len))
    fun section(offset: Int, len: Int) =
        StreamReader(fuckMyLife.copyOfRange(offset, offset + len))
    fun section(offset: Int) = StreamReader(read(offset, length - offset - 1))

    fun toByteArray() = fuckMyLife
    override fun toString() = String(fuckMyLife)

}
