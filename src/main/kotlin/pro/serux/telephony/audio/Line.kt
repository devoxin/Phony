package pro.serux.telephony.audio

import net.dv8tion.jda.api.audio.*
import net.dv8tion.jda.api.entities.User
import tomp2p.opuswrapper.Opus
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer


// Speaker can be null where there is no assigned individual to receive audio from.
// In this instance, audio from everyone speaking is consolidated.
class Line(var speaker: Long?) : AudioReceiveHandler, AudioSendHandler {
    private val segments = mutableListOf<ByteBuffer>()
    private val mixer = StereoPcmAudioMixer(960, true)

    override fun handleEncodedAudio(packet: OpusPacket) {
        if (speaker != null && packet.userId == speaker) {
            segments.add(ByteBuffer.wrap(packet.opusAudio))
        }
    }

    override fun handleCombinedAudio(combinedAudio: CombinedAudio) {
        if (speaker == null) {
            mixer.add(combinedAudio.getAudioData(1.0))
        }
    }

    override fun includeUserInCombinedAudio(user: User) = user.idLong != user.jda.selfUser.idLong

    // Send Handler
    override fun provide20MsAudio() = if (isOpus) segments.removeAt(0) else ByteBuffer.wrap(mixer.get())
    override fun canProvide() = if (isOpus) segments.size > 0 else mixer.hasData
    override fun isOpus() = speaker != null

    // Receive Handler
    override fun canReceiveEncoded() = true
    override fun canReceiveCombined() = true
}
