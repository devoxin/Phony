package pro.serux.telephony.commands

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import pro.serux.telephony.Database
import pro.serux.telephony.Loader
import pro.serux.telephony.audio.Line
import pro.serux.telephony.audio.LineTone
import pro.serux.telephony.entities.CallManager
import pro.serux.telephony.entities.CallStatus
import pro.serux.telephony.misc.Experiments
import pro.serux.telephony.parsers.memberorempty.Member

class Telephone : Cog {
    @Command(description = "Starts a call with another server.", guildOnly = true)
    fun call(ctx: Context, serverId: Long) {
        if (serverId == ctx.guild!!.idLong) {
            return ctx.send("You can't call this server.")
        }

        val server = Loader.shardManager.getGuildById(serverId)
            ?: return ctx.send("No guild with that ID.")

        if (callManager.isLineEngaged(ctx.guild!!.idLong)) {
            return ctx.send("The line for this server is already engaged.")
        }

        if (callManager.isLineEngaged(serverId)) {
            return ctx.send("That server's line is already engaged.")
        }

        server.getTextChannelsByName("phone", true).firstOrNull()
            ?: return ctx.send("That server doesn't have a `phone` channel.")

        val voiceChannel = ctx.member!!.voiceState?.channel
            ?: return ctx.send("You must be in a voice channel to make a call.")

        ctx.guild!!.audioManager.apply {
            openAudioConnection(voiceChannel)
            sendingHandler = LineTone()
        }

        val sanitizedName = ctx.cleanContent(ctx.guild!!.name)
        val call = callManager.setup(ctx.author.idLong, ctx.guild!!.idLong, serverId)
        call.notifyCaller("Calling...")
        call.notifyReceiver("Incoming call from **$sanitizedName**. " +
                "Accept with `${ctx.trigger}answer`, reject with `${ctx.trigger}reject`.")
    }

    @Command(description = "Answers an incoming call.", guildOnly = true)
    fun answer(ctx: Context) {
        val incomingCall = callManager.getIncomingCall(ctx.guild!!.idLong)
            ?: return ctx.send("There are no incoming calls.")

        val voiceChannel = ctx.member!!.voiceState?.channel
            ?: return ctx.send("You must be in a voice channel to accept the call.")

        val callerGuild = Loader.shardManager.getGuildById(incomingCall.callerGuildId)
            ?: return

        incomingCall.answer()

        val callerAm = callerGuild.audioManager
        val receiverAm = ctx.guild!!.audioManager

        receiverAm.openAudioConnection(voiceChannel)
        incomingCall.setReceivingSpeaker(ctx.author.idLong)

        // Setup lines
        val callerLine = Line(incomingCall.callerId)
        val receiverLine = Line(ctx.author.idLong)

        callerAm.receivingHandler = callerLine
        receiverAm.sendingHandler = callerLine

        receiverAm.receivingHandler = receiverLine
        callerAm.sendingHandler = receiverLine

        incomingCall.setCallerLine(callerLine)
        incomingCall.setReceiverLine(receiverLine)

        incomingCall.notifyCaller("**${ctx.author.asTag}** picked up.\nConnection established.")
        incomingCall.notifyReceiver("Connection established. Use `${ctx.trigger}hangup` at any time to end the call.")
    }

    @Command(aliases = ["reject", "end"], description = "Ends the call for this server.", guildOnly = true)
    fun hangup(ctx: Context) {
        if (!callManager.isLineEngaged(ctx.guild!!.idLong)) {
            return ctx.send("There is no outgoing/incoming call.")
        }

        val call = callManager.getCallFor(ctx.guild!!.idLong)
            ?: return

        if (call.status == CallStatus.CALLING && call.receiverGuildId == ctx.guild!!.idLong) {
            return call.reject()
        }

        call.end()

        if (call.isCaller(ctx.guild!!.idLong)) {
            call.notifyReceiver("**${ctx.author.asTag}** ended the call.")
            call.notifyCaller("Call ended.")
        } else {
            call.notifyCaller("**${ctx.author.asTag}** ended the call.")
            call.notifyReceiver("Call ended.")
        }

        callManager.remove(call)
    }

    @Command(aliases = ["speaker"], description = "Pass the phone to another user.", guildOnly = true)
    fun handover(ctx: Context, speaker: Member?) {
        if (!callManager.isLineEngaged(ctx.guild!!.idLong)) {
            return ctx.send("The phone is not active.")
        }

        val call = callManager.getCallFor(ctx.guild!!.idLong)
            ?: return ctx.send("Could not get call for this server.")

        val channelId = ctx.guild!!.audioManager.connectedChannel!!.idLong

        when {
            speaker == null -> {
                val value = Database.experimentsFor(ctx.guild!!.idLong)

                if (!Experiments.MULTI_USER_CALLS.isEnabled(value)) {
                    return ctx.send(
                        "Experiment `MULTI_USER_CALLS` is not enabled for this server, so " +
                                "you cannot set the phone to listen to everyone in the channel.\n" +
                                "Mention a user to pass the phone to them, or enable the experiment."
                    )
                }

                call.setSpeakerFor(ctx.guild!!.idLong, null)
                ctx.send("Speakerphone enabled. **Everyone** may now speak into the phone.")
            }
            speaker.parseFailed -> return ctx.send("`${speaker.arg}` could not be resolved into a `Member`.")
            else -> {
                val member = speaker.member!!

                if (channelId != member.voiceState?.channel?.idLong) {
                    return ctx.send("You can't pass the phone to someone not in the same voice channel.")
                }

                call.setSpeakerFor(ctx.guild!!.idLong, member.idLong)
                ctx.send("**${member.user.asTag}** now has the phone.")
            }
        }
    }

    companion object {
        val callManager = CallManager()
    }
}
