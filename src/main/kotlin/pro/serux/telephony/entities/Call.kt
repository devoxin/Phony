package pro.serux.telephony.entities

import pro.serux.telephony.Loader
import pro.serux.telephony.audio.Clip
import pro.serux.telephony.audio.Line
import pro.serux.telephony.commands.Telephone
import pro.serux.telephony.io.DCAReader
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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

    private val receiverHasAnswerMachine = File("${receiverGuildId}.dca").exists()

    init {
        timer.schedule({
            if (status == CallStatus.CALLING) {
                reject()
            }
        }, 30, TimeUnit.SECONDS)
    }

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

    fun reject() {
        val response = if (receiverHasAnswerMachine) "☎️ Connected to answering machine." else "Call ended; nobody answered."
        notifyCaller(response)
        notifyReceiver("Call dropped.")

        playVoicemail().whenComplete { _, _ ->
            end()
            Telephone.callManager.remove(this)
        }
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

    fun playVoicemail(): CompletableFuture<Nothing> {
        val future = CompletableFuture<Nothing>()

        if (!receiverHasAnswerMachine) {
            future.complete(null)
            return future
        }

        status = CallStatus.ANSWERING_MACHINE

        val audio = DCAReader().loadDcaFile(File("${receiverGuildId}.dca").inputStream())
        val clip = Clip(audio.opusAudioChunks)
        val callerGuild = Loader.shardManager.getGuildById(callerGuildId)
        clip.finishTask = {
            callerGuild?.audioManager?.sendingHandler = null
            future.complete(null)
        }

        if (callerGuild == null) {
            future.complete(null)
            return future
        }

        callerGuild.audioManager.sendingHandler = clip
        return future
    }

    private fun notify(guildId: Long, content: String) {
        Loader.shardManager.getGuildById(guildId)
            ?.getTextChannelsByName("phone", true)
            ?.firstOrNull()
            ?.sendMessage(content)
            ?.queue()
    }

    companion object {
        private val timer = Executors.newSingleThreadScheduledExecutor()
    }
}
