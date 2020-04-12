package pro.serux.telephony.audio

import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class LineTone : AudioSendHandler {
    private val toneBuffer = DCAReader.opusAudioChunks

    override fun provide20MsAudio(): ByteBuffer {
        println("providing")
        return ByteBuffer.wrap(toneBuffer.removeAt(0))
    }
    override fun canProvide(): Boolean {
        println("can provide: ${toneBuffer.size > 0}")
        return toneBuffer.size > 0
    }
    override fun isOpus() = true
}
