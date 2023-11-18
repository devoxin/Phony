package pro.serux.telephony.commands

import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Cog
import pro.serux.telephony.Database
import pro.serux.telephony.misc.Experiments

class Experiments : Cog {
    @Command(aliases = ["showexp", "exp"], description = "Shows all available experiments.", guildOnly = true)
    fun experiments(ctx: Context) {
        val maxLength = Experiments.values().map { it.name.length }.max()!!
        val optedIn = Database.experimentsFor(ctx.guild!!.idLong)

        val experiments = buildString {
            for (ex in Experiments.values()) {
                if (ex.isEnabled(optedIn)) {
                    append("✓ ")
                }

                append("`")
                append(ex.name.padEnd(maxLength, ' '))
                append(":` ")
                appendln(ex.description)
            }
        }

        ctx.send {
            setTitle("Available Experiments")
            setDescription(
                "Experiments are prone to breaking, and could affect your experience.\n" +
                        "You should only enable these if you know what you're doing.\n\n$experiments"
            )
        }
    }

    @Command(aliases = ["ee"], description = "Enables an experiment for this server.", guildOnly = true)
    fun enableexperiment(ctx: Context, experiment: String) {
        val ex = Experiments.values().firstOrNull { it.name == experiment }
            ?: return ctx.send("That experiment doesn't exist.")

        if (isOptedInto(ex, ctx.guild!!.idLong)) {
            return ctx.send("You're already opted into `${ex.name}`")
        }

        optInto(ex, ctx.guild!!.idLong)
        ctx.send("Successfully opted into `${ex.name}`.")
    }

    @Command(aliases = ["de"], description = "Disables an experiment for this server.", guildOnly = true)
    fun disableexperiment(ctx: Context, experiment: String) {
        val ex = Experiments.values().firstOrNull { it.name == experiment }
            ?: return ctx.send("That experiment doesn't exist.")

        if (!isOptedInto(ex, ctx.guild!!.idLong)) {
            return ctx.send("You're not opted into `${ex.name}`")
        }

        optOutOf(ex, ctx.guild!!.idLong)
        ctx.send("Successfully opted out of `${ex.name}`.")
    }

    private fun isOptedInto(experiment: Experiments, guildId: Long) = experiment.isEnabled(Database.experimentsFor(guildId))

    private fun optInto(experiment: Experiments, guildId: Long) {
        val value = Database.experimentsFor(guildId) or experiment.rawValue
        Database.setExperiments(guildId, value)
    }

    private fun optOutOf(experiment: Experiments, guildId: Long) {
        val value = Database.experimentsFor(guildId) and experiment.rawValue.inv()
        Database.setExperiments(guildId, value)
    }
}
