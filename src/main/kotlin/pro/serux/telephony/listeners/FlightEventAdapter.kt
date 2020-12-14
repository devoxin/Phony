package pro.serux.telephony.listeners

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.hooks.DefaultCommandEventAdapter
import net.dv8tion.jda.api.Permission

class FlightEventAdapter : DefaultCommandEventAdapter() {
    private fun rootCauseOf(ex: Throwable): Throwable {
        return ex.cause?.let(::rootCauseOf) ?: ex
    }

    override fun onBadArgument(ctx: Context, command: CommandFunction, error: BadArgument) {
        ctx.send("You must provide an argument for `${error.argument.name}`")
    }

    override fun onCommandError(ctx: Context, command: CommandFunction, error: Throwable) {
        val cause = rootCauseOf(error)
        ctx.send("oop\n```\n${cause.message}```")
        error.printStackTrace()
    }

    override fun onCommandPostInvoke(ctx: Context, command: CommandFunction, failed: Boolean) {
        println("Command ${command.name} finished execution. Failed: $failed")
    }

    @ExperimentalStdlibApi
    override fun onCommandPreInvoke(ctx: Context, command: CommandFunction): Boolean {
        return true
    }

    override fun onParseError(ctx: Context, command: CommandFunction, error: Throwable) {
        ctx.send("An error occurred during argument parsing.\n```\n$error```")
        error.printStackTrace()
    }

    override fun onBotMissingPermissions(ctx: Context, command: CommandFunction, permissions: List<Permission>) {

    }

    override fun onUserMissingPermissions(ctx: Context, command: CommandFunction, permissions: List<Permission>) {

    }
}
