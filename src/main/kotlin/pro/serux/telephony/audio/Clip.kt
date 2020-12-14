package pro.serux.telephony.audio

import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class Clip(private val buffer: MutableList<ByteArray>) : AudioSendHandler {
    var finishTask: () -> Unit = {}
    private var taskInvoked = false

    override fun provide20MsAudio() = ByteBuffer.wrap(buffer.removeAt(0))
    override fun canProvide(): Boolean {
        val hasAudio = buffer.size > 0

        if (!hasAudio && !taskInvoked) {
            finishTask()
            taskInvoked = true
        }

        return hasAudio
    }
    override fun isOpus() = true
}
