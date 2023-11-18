package pro.serux.telephony.io

import java.io.File
import java.io.FileOutputStream

// Class for writing DCA0 files.
class DCAWriter(fileName: String) {
    private val writer = StreamWriter(File(fileName).outputStream())

    // TODO: Filter silence

    fun addOpusSegment(byteArray: ByteArray) {
        writer.writeInt16LE(byteArray.size)
        writer.writeArray(byteArray)
    }

    fun finish() {
        writer.finish()
    }
}
