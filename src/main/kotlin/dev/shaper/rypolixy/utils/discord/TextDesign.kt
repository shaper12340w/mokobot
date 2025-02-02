package dev.shaper.rypolixy.utils.discord

import dev.kord.rest.builder.message.EmbedBuilder

object TextDesign {
    object Embed {
        fun EmbedBuilder.title(emoji:String, text:String){
            this.title = "$emoji $text"
        }
        fun EmbedBuilder.description(text:String?){
            if(text != null)
                this.description = "```\n$text\n```"
        }
    }
}