package pro.serux.telephony.entities

import pro.serux.telephony.Loader
import pro.serux.telephony.audio.Line

class Call(
    val callerId: Long,
    val callerGuildId: Long,
    val receiverGuildId: Long
) {
    var callingSpeaker: Long? = callerId
        private set

    var receivingSpeaker: Long? = null
        private set

    private var callerLine: Line? = null
    private var receiverLine: Line? = null

    var status = CallStatus.CALLING
        private set

    fun isCaller(guildId: Long): Boolean {
        return callerGuildId == guildId
    }

    fun setCallerLine(line: Line) {
        callerLine = line
    }

    fun setReceiverLine(line: Line) {
        receiverLine = line
    }

    fun notifyCaller(content: String) = notify(callerGuildId, content)
    fun notifyReceiver(content: String) = notify(receiverGuildId, content)

    fun setSpeakerFor(guildId: Long, speaker: Long?) {
        if (isCaller(guildId)) {
            setCallingSpeaker(speaker)
        } else {
            setReceivingSpeaker(speaker)
        }
    }

    fun setCallingSpeaker(userId: Long?) {
        callingSpeaker = userId
        callerLine?.speaker = userId
    }

    fun setReceivingSpeaker(userId: Long?) {
        receivingSpeaker = userId
        receiverLine?.speaker = userId
    }

    fun answer() {
        status = CallStatus.ANSWERED
    }

    fun end() {
        status = CallStatus.ENDED

        val callerGuild = Loader.shardManager.getGuildById(callerGuildId)
        val receiverGuild = Loader.shardManager.getGuildById(receiverGuildId)

        callerGuild?.audioManager?.apply {
            receivingHandler = null
            sendingHandler = null
            closeAudioConnection()
        }

        receiverGuild?.audioManager?.apply {
            receivingHandler = null
            sendingHandler = null
            closeAudioConnection()
        }
    }

    private fun notify(guildId: Long, content: String) {
        Loader.shardManager.getGuildById(guildId)
            ?.getTextChannelsByName("phone", true)
            ?.firstOrNull()
            ?.sendMessage(content)
            ?.queue()
    }

}
