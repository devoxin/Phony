package pro.serux.telephony.audio

import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class Clip(private val buffer: MutableList<ByteArray>) : AudioSendHandler {
    override fun provide20MsAudio() = ByteBuffer.wrap(buffer.removeAt(0))
    override fun canProvide() = buffer.size > 0
    override fun isOpus() = true
}
