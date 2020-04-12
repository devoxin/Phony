package pro.serux.telephony.audio

import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.OpusPacket
import pro.serux.telephony.io.DCAWriter
import kotlin.math.floor

class Recorder(
    private val dcaWriter: DCAWriter,
    private val speaker: Long
) : AudioReceiveHandler {
    private var lastReceived = 0L

    override fun handleEncodedAudio(packet: OpusPacket) {
        if (packet.userId == speaker) {
            checkSilence()
            dcaWriter.addOpusSegment(packet.opusAudio)
        }
    }

    private fun checkSilence() {
        if (lastReceived == 0L) {
            lastReceived = System.currentTimeMillis()
            return
        }

        val elapsedMs = (System.currentTimeMillis() - lastReceived - 40).coerceAtLeast(0)
        val silenceFrames = elapsedMs / 20

        lastReceived = System.currentTimeMillis()

        if (elapsedMs == 0L || silenceFrames == 0L) {
            return
        }

        for (x in 0 until silenceFrames) {
            dcaWriter.addOpusSegment(silenceBytes)
        }
    }

    override fun canReceiveEncoded() = true

    companion object {
        private val silenceBytes = byteArrayOf(0xF8.toByte(), 0xFF.toByte(), 0xFE.toByte())
    }
}
