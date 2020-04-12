package pro.serux.telephony.audio

import net.dv8tion.jda.api.audio.AudioSendHandler
import pro.serux.telephony.Loader
import java.nio.ByteBuffer

class LineTone : AudioSendHandler {
    private val toneBuffer = Loader.dialToneDca.opusAudioChunks

    override fun provide20MsAudio() = ByteBuffer.wrap(toneBuffer.removeAt(0))
    override fun canProvide() = toneBuffer.size > 0
    override fun isOpus() = true
}
