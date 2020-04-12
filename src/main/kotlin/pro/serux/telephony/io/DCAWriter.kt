package pro.serux.telephony.io

import java.io.File

class DCAWriter(fileName: String) {

    private val file = File(fileName)

    private val writer = StreamWriter()
    private val segments = mutableListOf<ByteArray>()

    // TODO: Filter silence

    fun addOpusSegment(byteArray: ByteArray) {
        segments.add(byteArray)
    }

    fun save() {
        for (segment in segments) {
            writer.writeInt16LE(segment.size)
            writer.writeArray(segment)
        }

        val output = writer.flush()
        file.writeBytes(output)
    }

}
