package de.runebot.behaviors

import de.runebot.Util
import dev.kord.core.event.message.MessageCreateEvent

object TechnikBehavior : Behavior
{
    override suspend fun run(content: String, messageCreateEvent: MessageCreateEvent)
    {
        if (content.contains("technik", ignoreCase = true))
        {
            Util.sendMessage(messageCreateEvent, "DIE TECHNIK, THADDÄUS!")
        }
    }
}