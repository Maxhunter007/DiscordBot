package de.runebot.commands

import de.runebot.Util
import de.runebot.database.DB
import de.runebot.database.DBResponse
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent

object TagCommand : MessageCommand
{
    override val names: List<String>
        get() = listOf("tag", "t")
    override val shortHelpText: String
        get() = "create/get saved tags"
    override val longHelpText: String
        get() = "`$commandExample tagName`: Display tag.\n" +
                "`$commandExample create tagName tagContent`: Create tag.\n" +
                "`$commandExample update tagName tagContent`: Update tag.\n" +
                "`$commandExample delete tagName`: Delete tag."
    private lateinit var kord: Kord

    override fun prepare(kord: Kord)
    {
        this.kord = kord
        println("Tag command ready.")
    }

    override suspend fun execute(event: MessageCreateEvent, args: List<String>)
    {
        val creatorId = event.message.author?.id?.value
        if (creatorId == null)
        {
            Util.sendMessage(event, "Couldn't process request - Missing user ID")
            return
        }
        if (args.size <= 1)
        {
            Util.sendMessage(event, "Try >(`${names.joinToString("` | `")}`) `help`")
            return
        }
        when (args[1])
        {
            "create" ->
            {
                if (args.size <= 2)
                {
                    sendTag(event, args[1])
                    return
                }
                else if (args.size <= 3)
                {
                    Util.sendMessage(event, "Cannot create empty tag.")
                    return
                }
                val tagCreateResponse = DB.storeTag(
                    args[2],
                    args.subList(3, args.size).joinToString(" "),
                    event.message.author?.id?.value?.toLong() ?: 0
                )
                when (tagCreateResponse)
                {
                    DBResponse.SUCCESS ->
                    {
                        Util.sendMessage(event, "Tag successfully created.")
                    }

                    DBResponse.FAILURE ->
                    {
                        Util.sendMessage(event, "Tag could not be created.")
                    }

                    else ->
                    {
                        Util.sendMessage(event, "Something unexpected happened. Ping a RuneBot admin.")
                    }
                }
            }

            "update" ->
            {
                if (args.size <= 2)
                {
                    sendTag(event, args[1])
                    return
                }
                else if (args.size <= 3)
                {
                    Util.sendMessage(event, "Cannot make tag empty.")
                    return
                }
                val tagUpdateResponse = if (MessageCommand.isAdmin(event)) DB.updateTag(
                    args[2],
                    args.subList(3, args.size).joinToString(" "),
                    event.message.author?.id?.value?.toLong() ?: 0
                )
                else DB.updateTagIfOwner(
                    args[2],
                    args.subList(3, args.size).joinToString(" "),
                    event.message.author?.id?.value?.toLong() ?: 0
                )
                when (tagUpdateResponse)
                {
                    DBResponse.SUCCESS ->
                    {
                        Util.sendMessage(event, "Tag successfully updated.")
                    }

                    DBResponse.FAILURE ->
                    {
                        Util.sendMessage(event, "Tag could not be updated.")
                    }

                    DBResponse.WRONG_USER ->
                    {
                        Util.sendMessage(event, "You do not own this tag.")
                    }

                    DBResponse.MISSING_ENTRY ->
                    {
                        Util.sendMessage(event, "Tag doesn't exist.")
                    }
                }
            }

            "delete" ->
            {
                if (args.size <= 2)
                {
                    sendTag(event, args[1])
                    return
                }
                val tagDeleteResponse = if (MessageCommand.isAdmin(event)) DB.deleteTag(
                    args[2]
                )
                else DB.deleteTagIfOwner(
                    args[2],
                    creatorId.toLong()
                )
                when (tagDeleteResponse)
                {
                    DBResponse.SUCCESS ->
                    {
                        Util.sendMessage(event, "Tag successfully deleted.")
                    }

                    DBResponse.FAILURE ->
                    {
                        Util.sendMessage(event, "Tag could not be deleted.")
                    }

                    DBResponse.WRONG_USER ->
                    {
                        Util.sendMessage(event, "You do not own this tag.")
                    }

                    DBResponse.MISSING_ENTRY ->
                    {
                        Util.sendMessage(event, "Tag doesn't exist.")
                    }
                }
            }

            else ->
            {
                sendTag(event, args[1])
            }
        }
    }

    private suspend fun sendTag(event: MessageCreateEvent, tagName: String)
    {
        DB.getTag(tagName)?.let {
            Util.sendMessage(event, it)
        } ?: Util.sendMessage(event, "Tag doesn't exist.")
    }
}