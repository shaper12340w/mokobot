package dev.shaper.rypolixy.utils.discord.context

import dev.kord.common.entity.MessageFlags
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.AttachmentBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageBuilder

data class DefaultMessageBuilder(
    override var content: String? = null,
    override var allowedMentions: AllowedMentionsBuilder? = null,
    override var attachments: MutableList<AttachmentBuilder>? = null,
    override var components: MutableList<MessageComponentBuilder>? = null,
    override var embeds: MutableList<EmbedBuilder>? = null,
    override val files: MutableList<NamedFile> = mutableListOf(),
    override var flags: MessageFlags? = null,
    override var suppressEmbeds: Boolean? = null
) : MessageBuilder