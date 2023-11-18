package pro.serux.telephony.commands

import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Cog
import pro.serux.telephony.Database
import pro.serux.telephony.Loader

class Misc : Cog {
    private fun abbreviate(s: String): String {
        if (s.length > 25) {
            return s.substring(0, 22) + "..."
        }

        return s
    }

    @Command(description = "Displays the bot invite link, and server support link.")
    fun invite(ctx: Context) {
        ctx.send("Add the bot -> <https://discordapp.com/oauth2/authorize?client_id=685211474192498741&scope=bot>\n" +
                "Join the support server -> https://discord.gg/xvtH2Yn")
    }

    @Command(description = "Getting started with the bot.")
    fun start(ctx: Context) {
        ctx.send("• Create a text channel called `phone`. This will be used to output call information.\n" +
                "• Enable developer settings (User Settings > Appearance > Developer Mode: Toggle it on).\n" +
                "• Find a server you want to call (make sure it has this bot!)\n" +
                "• Right-click the server's icon > Copy ID\n" +
                "• Paste the server ID into the call command (`${ctx.trigger}call <ID HERE>` (without the <>))\n" +
                "• Wait for someone in the other server to run `${ctx.trigger}answer`\n\n" +
                "A public directory will be coming soon which will make it easier to discover guilds for you to call.\n" +
                "If you need any help with the bot (i.e. if you have encountered the bug or don't understand these " +
                "instructions), run the `${ctx.trigger}invite` command and join the server!")
    }

    @Command(aliases = ["directory"], description = "Lists all servers who have opted in to the phonebook.", guildOnly = true)
    fun phonebook(ctx: Context) {
        val guilds = Database.phonebookList().mapNotNull(Loader.shardManager::getGuildById)
        val builder = StringBuilder("```\n")

        builder.append(String.format("%-25s | Number\n", "Server Name"))
        builder.append("-".repeat(46))
        builder.append("\n")

        for (guild in guilds) {
            builder.append(String.format("%-25s   %s\n", abbreviate(guild.name), guild.id))
        }

        builder.append("```")
        ctx.send(builder.toString())
    }

    @Command(description = "Opt-in to having your server discoverable in the phonebook.", guildOnly = true)
    fun optin(ctx: Context) {
        Database.phonebookOptIn(ctx.guild!!.idLong)
        ctx.send("Done. This server is now discoverable.")
    }

    @Command(description = "Opt-out to having your server discoverable in the phonebook.", guildOnly = true)
    fun optout(ctx: Context) {
        Database.phonebookOptOut(ctx.guild!!.idLong)
        ctx.send("Done. This server is no longer discoverable.")
    }

    @Command(description = "Displays bot information.")
    fun info(ctx: Context) {
        ctx.send {
            setColor(0xCD3B3B)
            setTitle("Phony | A cross-server voice communication bot")
            addField("Servers", Loader.shardManager.guildCache.size().toString(), false)
            setFooter("Developed by devoxin")
        }
    }
}
