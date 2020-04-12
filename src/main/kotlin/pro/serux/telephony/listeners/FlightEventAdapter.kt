package pro.serux.telephony.listeners

import me.devoxin.flight.api.CommandWrapper
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.DefaultCommandClientAdapter
import me.devoxin.flight.exceptions.BadArgument
import net.dv8tion.jda.api.Permission

class FlightEventAdapter : DefaultCommandClientAdapter() {

    fun rootCauseOf(ex: Throwable): Throwable {
        return ex.cause?.let(::rootCauseOf) ?: ex
    }

    override fun onBadArgument(ctx: Context, command: CommandWrapper, error: BadArgument) {
        ctx.send("You must provide an argument for `${error.argument.name}`")
    }

    override fun onCommandError(ctx: Context, command: CommandWrapper, error: Throwable) {
        val cause = rootCauseOf(error)
        ctx.send("oop\n```\n${cause.message}```")
        error.printStackTrace()
    }

    override fun onCommandPostInvoke(ctx: Context, command: CommandWrapper, failed: Boolean) {
        println("Command ${command.name} finished execution. Failed: $failed")
    }

    @ExperimentalStdlibApi
    override fun onCommandPreInvoke(ctx: Context, command: CommandWrapper): Boolean {
        return true
    }

    override fun onParseError(ctx: Context, command: CommandWrapper, error: Throwable) {
        ctx.send("An error occurred during argument parsing.\n```\n$error```")
        error.printStackTrace()
    }

    override fun onBotMissingPermissions(ctx: Context, command: CommandWrapper, permissions: List<Permission>) {

    }

    override fun onUserMissingPermissions(ctx: Context, command: CommandWrapper, permissions: List<Permission>) {

    }

}