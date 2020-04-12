package pro.serux.telephony.audio

import pro.serux.telephony.misc.StreamBuffer

object DCAReader {

    private var hasHead = false
    private val opusSegments = mutableListOf<ByteArray>()

    val opusAudioChunks: MutableList<ByteArray>
        get() = opusSegments.toMutableList()

    fun loadDcaFile(filename: String) {
        val stream = this::class.java.classLoader.getResourceAsStream(filename)
            ?: throw IllegalStateException("Could not get input stream for filename $filename")

        val buffer = StreamBuffer(stream)
        readHead(buffer)
    }

    private fun readHead(streamBuffer: StreamBuffer) {
        val dcaVersion = streamBuffer.section(0, 4)

        if (dcaVersion[0] != 68 || dcaVersion[1] != 67 || dcaVersion[2] != 65) {
            this.hasHead = true
            processChunk(streamBuffer)
        } else if (dcaVersion[3] == 49) {
            val jsonLength = streamBuffer.section(4, 8).readUInt32LE(0)
            val jsonMetadata = streamBuffer.section(8, 8 + jsonLength)
            this.hasHead = true
            processChunk(streamBuffer.section(8 + jsonLength))
        } else {
            throw IllegalStateException("Unknown DCA version!")
        }
    }

    fun processChunk(chunk: StreamBuffer) {
        val opusLen = chunk.readInt16LE(0)
        val segment = chunk.section(2, opusLen)
        opusSegments.add(segment.toByteArray())

        val nextChunkLength = chunk.length - opusLen - 2
        val nextChunk = chunk.section(2 + opusLen, nextChunkLength)

        if (nextChunk.length > 0) {
            processChunk(nextChunk)
        }
    }
}
