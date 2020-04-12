package pro.serux.telephony.commands

import me.devoxin.flight.annotations.Command
import me.devoxin.flight.api.Context
import me.devoxin.flight.models.Cog
import pro.serux.telephony.audio.Clip
import pro.serux.telephony.audio.Recorder
import pro.serux.telephony.io.DCAReader
import pro.serux.telephony.io.DCAWriter
import java.io.File

class Voicemail : Cog {

    private val recorders = hashMapOf<Long, DCAWriter>()

    @Command(description = "Record a voicemail message.")
    fun setmessage(ctx: Context) {
        if (recorders.containsKey(ctx.guild!!.idLong)) {
            return ctx.send("no")
        }

        val writer = DCAWriter("${ctx.guild!!.id}.dca")
        val recorder = Recorder(writer, ctx.author.idLong)

        ctx.guild!!.audioManager.apply {
            openAudioConnection(ctx.member!!.voiceState!!.channel!!)
            receivingHandler = recorder
        }

        recorders[ctx.guild!!.idLong] = writer
    }

    @Command(description = "Stops voicemail message recording.")
    fun stoprecording(ctx: Context) {
        if (!recorders.containsKey(ctx.guild!!.idLong)) {
            return ctx.send("Not recording")
        }

        recorders[ctx.guild!!.idLong]?.save()
        ctx.guild!!.audioManager.apply {
            receivingHandler = null
            closeAudioConnection()
        }

        recorders.remove(ctx.guild!!.idLong)
        ctx.send("Stopped and saved.")
    }

    @Command(description = "Plays the current voicemail message.")
    fun playmessage(ctx: Context) {
        val message = File("${ctx.guild!!.id}.dca")

        if (!message.exists()) {
            return ctx.send("This server does not have a voicemail recording.")
        }

        val stream = message.inputStream()
        val reader = DCAReader().loadDcaFile(stream)

        val clip = Clip(reader.opusAudioChunks)

        ctx.guild!!.audioManager.apply {
            openAudioConnection(ctx.member!!.voiceState!!.channel!!)
            sendingHandler = clip
        }
    }

}
