package pro.serux.telephony.listeners

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.hooks.EventListener
import pro.serux.telephony.commands.Telephone
import pro.serux.telephony.entities.CallStatus

class EmptyVcListener : EventListener {

    override fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildVoiceMoveEvent -> checkEmptyVoice(event.guild)
            is GuildVoiceLeaveEvent -> checkEmptyVoice(event.guild)
        }
    }

    private fun checkEmptyVoice(guild: Guild) {
        val call = Telephone.callManager.getCallFor(guild.idLong)
        val callChannel = guild.audioManager.connectedChannel?.takeIf { it.members.none { m -> !m.user.isBot } }

        if (call != null && call.status != CallStatus.CALLING && callChannel == null) {
            call.notifyCaller("Call ended; the phone was abandoned.")
            call.notifyReceiver("Call ended; the phone was abandoned.")
            call.end()
            Telephone.callManager.remove(call)
        }
    }

}
