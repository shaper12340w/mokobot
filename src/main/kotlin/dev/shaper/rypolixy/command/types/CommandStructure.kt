package dev.shaper.rypolixy.command.types

import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

interface CommandStructure{

    val name       :String

    val description:String

    val enabled    :Boolean?

}

/*
* Message Command [ MessageCommandInteractionCreateEvent ] : 메세지 우클릭시 명령 실행
*
* Interaction Command [ ChatInputCommandInteractionCreateEvent ] : 슬레시 명령어
*
* Text Command [ MessageCreateEvent ] : 메세지 전송시 메세지 감지 명령어
*
* */