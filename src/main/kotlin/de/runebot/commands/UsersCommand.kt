package de.runebot.commands

import de.runebot.Util
import de.runebot.config.Config
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.event.message.MessageCreateEvent

object UsersCommand : MessageCommand
{
    override val names: List<String>
        get() = listOf("users", "us")
    override val needsAdmin: Boolean
        get() = true

    private lateinit var kord: Kord

    override fun prepare(kord: Kord)
    {
        this.kord = kord
        println("Users command ready.")
    }

    override suspend fun execute(event: MessageCreateEvent, args: List<String>)
    {
        Config.get("adminChannel")?.let { channelID ->
            val users = StringBuilder()
            event.getGuild()?.let { guild ->
                users.append("Users on ${guild.name}")
                guild.members.collect { member ->
                    users.append("\n - ${member.displayName} (${member.username})")
                }
            }
            Util.sendMessage(MessageChannelBehavior(Snowflake(channelID), kord), users.toString())
        } ?: Util.sendMessage(event, "Admin channel has not been set yet!")
    }
}