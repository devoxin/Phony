package pro.serux.telephony.io

import java.io.InputStream

class DCAReader {
    private var hasHead = false
    private val opusSegments = mutableListOf<ByteArray>()

    val opusAudioChunks: MutableList<ByteArray>
        get() = opusSegments.toMutableList()

    fun loadDcaFile(filename: String): DCAReader {
        val stream = this::class.java.classLoader.getResourceAsStream(filename)
            ?: throw IllegalStateException("Could not get input stream for filename $filename")

        return loadDcaFile(stream)
    }

    fun loadDcaFile(inputStream: InputStream): DCAReader {
        val buffer = StreamReader(inputStream)
        process(buffer)
        return this
    }

    private fun process(reader: StreamReader) {
        // readHead returns StreamReader! but processChunk returns StreamReader?.
        var nextChunk: StreamReader? = readHead(reader)

        while (nextChunk != null) {
            nextChunk = processChunk(nextChunk)
        }
    }

    private fun readHead(reader: StreamReader): StreamReader {
        val dcaVersion = reader.section(0, 4)

        // DCA0 doesn't have magic bytes, so we check if the header does NOT equal "DCA".
        return if (dcaVersion[0] != 68 || dcaVersion[1] != 67 || dcaVersion[2] != 65) { // DCA0
            hasHead = true
            reader
        // If it does, we check if the version number at index 3 is a "1", for DCA1.
        } else if (dcaVersion[3] == 49) { // DCA1
            val jsonLength = reader.section(4, 8).readUInt32LE(0)
            val jsonMetadata = reader.section(8, 8 + jsonLength)
            hasHead = true
            reader.section(8 + jsonLength)
        } else {
            throw IllegalStateException("Unknown DCA version!")
        }
    }

    fun processChunk(chunk: StreamReader): StreamReader? {
        val opusLen = chunk.readInt16LE(0)
        val segment = chunk.section(2, opusLen)
        opusSegments.add(segment.toByteArray())

        val nextChunkLength = chunk.length - opusLen - 2
        val nextChunk = chunk.section(2 + opusLen, nextChunkLength)

        return nextChunk.takeIf { it.length > 0 }
    }

}
