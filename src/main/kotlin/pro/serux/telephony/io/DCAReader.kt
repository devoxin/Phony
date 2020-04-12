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
        var nextChunk: StreamReader? = readHead(reader)

        while (nextChunk != null) {
            nextChunk = processChunk(nextChunk)
        }
    }

    private fun readHead(reader: StreamReader): StreamReader {
        val dcaVersion = reader.section(0, 4)

        return if (dcaVersion[0] != 68 || dcaVersion[1] != 67 || dcaVersion[2] != 65) { // DCA0
            this.hasHead = true
            reader
        } else if (dcaVersion[3] == 49) { // DCA1
            val jsonLength = reader.section(4, 8).readUInt32LE(0)
            val jsonMetadata = reader.section(8, 8 + jsonLength)
            this.hasHead = true
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
