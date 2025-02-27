package dev.shaper.rypolixy.utils.discord.actionrow

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.ModalParentInteractionBehavior
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.component.*
import dev.shaper.rypolixy.utils.structure.eventemitter.Event
import dev.shaper.rypolixy.utils.structure.eventemitter.EventEmitter
import dev.shaper.rypolixy.utils.structure.eventemitter.EventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object ActionRowManager {

    val emitter = EventEmitter()

    class ButtonEvent       (val id:String, val interaction: ButtonInteraction)         : Event
    class ModalEvent        (val id:String, val interaction: ModalSubmitInteraction)    : Event
    class SelectMenuEvent   (val id:String, val interaction: SelectMenuInteraction)     : Event

    class CreateButton(private val customId: UUID, private val buttonData:ButtonData? = null) {

        private val buttonList = mutableListOf<ButtonInit>()
        private val listeners = mutableListOf<EventListener<ButtonEvent>>()

        init {
            buttonList.addAll(buttonData?.buttons ?: emptyList())
        }

        fun addButton(button: ButtonInit) {
            buttonList.add(button)
        }

        fun getListeners(): List<EventListener<ButtonEvent>> = listeners

        fun build(): ActionRowBuilder {
            fun createButtonSet(buttons: List<Button>) : ActionRowBuilder {
                val actionRow = ActionRowBuilder()
                buttons.forEachIndexed { index, button ->
                   val builder = if (button.url != null) {
                        ButtonBuilder.LinkButtonBuilder(
                            button.url!!,
                        ).apply {
                            if(button.emoji != null)     emoji = button.emoji
                            if(button.label != null)     label = button.label
                            if(button.disabled != null)  disabled = button.disabled
                        }
                   }
                   else {
                       ButtonBuilder.InteractionButtonBuilder(
                           button.style ?: ButtonStyle.Primary,
                           "$customId#$index",
                       ).apply{
                           if(button.emoji != null)     emoji = button.emoji
                           if(button.label != null)     label = button.label
                           if(button.disabled != null)  disabled = button.disabled
                       }
                   }
                   actionRow.components.add(builder)
                }
                return actionRow
            }

            buttonList.forEachIndexed { index, button ->
                val listener = object : EventListener<ButtonEvent> {
                    override fun onEvent(event: ButtonEvent) {
                        if (event.id == "$customId#$index") {
                            CoroutineScope(Dispatchers.IO).launch {
                                button.execute(
                                    event.interaction,
                                    { buttons -> editButton(buttons) },
                                    { killButton() }
                                )
                            }
                        }
                    }

                    fun editButton(newButtons: List<Button>): ActionRowBuilder {
                        newButtons.forEachIndexed { i, newBtn ->
                            if (i < buttonList.size) {
                                buttonList[i].apply {
                                    newBtn.disabled?.let { disabled = it }
                                    newBtn.emoji?.let { emoji = it }
                                    newBtn.label?.let { label = it }
                                    newBtn.style?.let { style = it }
                                    newBtn.url?.let { url = it }
                                }
                            }
                        }
                        return createButtonSet(buttonList)
                    }

                    fun killButton(isAll: Boolean = false) {
                        if (isAll)  listeners.forEach { emitter.off(it) }
                        else        emitter.off(this)
                    }
                }
                emitter.on(listener)
                listeners.add(listener)
            }
            return createButtonSet(buttonList)
        }
    }

    class CreateModal(private val customId: UUID, private val modalData: ModalData) {

        private val modalList = mutableListOf<TextInput>()

        init {
            modalList.addAll(modalData.inputs)
        }

        private fun createTextInput(data: TextInput,index:Int): TextInputBuilder {
            val builder = TextInputBuilder(
                style       = data.style,
                customId    = "$customId#$index",
                label       = data.label
            ).apply {
                disabled        = data.disabled
                required        = data.required
                allowedLength   = data.length
                placeholder     = data.placeHolder
                value           = data.value
            }
            return builder
        }

        fun addInput(input: TextInput) {
            modalList.add(input)
        }

        suspend fun show(
            interaction: ModalParentInteractionBehavior,
            execute     : suspend (ModalSubmitInteraction) -> Unit,
            ){
            val inputs = modalList.mapIndexed { index, input ->
                ActionRowBuilder().apply {
                    components.add(createTextInput(input, index))
                }
            }
            val listener = object : EventListener<ModalEvent> {
                override fun onEvent(event: ModalEvent) {
                    if(event.interaction.modalId.contains(customId.toString())) {
                        CoroutineScope(Dispatchers.IO).launch {
                            execute(event.interaction)
                        }
                    }
                }
            }
            interaction.modal(modalData.title,customId.toString()){ components.addAll(inputs) }
            emitter.once(listener)
        }
    }

    class CreateSelectMenu(private val customId: UUID,private val selectMenuData: SelectMenuData){
        private fun createStringSelectMenu(id:String, data:SelectMenu) : StringSelectBuilder{
            return StringSelectBuilder(id).apply {
                allowedValues   = data.length
                disabled        = data.disabled
                options         = data.options.filterIsInstance<SelectOptionBuilder>().toMutableList()
                placeholder     = data.placeHolder
            }
        }
        private fun createChannelSelectMenu(id:String, data:SelectMenu) : ChannelSelectBuilder{
            return ChannelSelectBuilder(id).apply {
                allowedValues   = data.length
                disabled        = data.disabled
                placeholder     = data.placeHolder
                defaultChannels.addAll(data.options.filterIsInstance<Snowflake>())
            }
        }
        private fun createUserSelectMenu(id:String, data:SelectMenu) : UserSelectBuilder{
            return UserSelectBuilder(id).apply {
                allowedValues   = data.length
                disabled        = data.disabled
                placeholder     = data.placeHolder
                defaultUsers.addAll(data.options.filterIsInstance<Snowflake>())
            }
        }
        private fun createRoleSelectMenu(id:String, data:SelectMenu) : RoleSelectBuilder{
            return RoleSelectBuilder(id).apply {
                allowedValues   = data.length
                disabled        = data.disabled
                placeholder     = data.placeHolder
                defaultRoles.addAll(data.options.filterIsInstance<Snowflake>())
            }
        }
        //private fun createMentionableSelectMenu(){}

        private fun listenerBuilder(menu: SelectMenu,index:Int) : EventListener<SelectMenuEvent> {
            val listener = object : EventListener<SelectMenuEvent> {
                fun kill() {
                    emitter.off(this)
                }
                override fun onEvent(event: SelectMenuEvent) {
                    if(event.id == "$customId#$index") {
                        CoroutineScope(Dispatchers.IO).launch {
                            menu.execute(event.interaction,::kill)
                        }
                    }
                }
            }
            return listener
        }

        fun build() : ActionRowBuilder {
            val builder =  ActionRowBuilder()
            selectMenuData.input.forEachIndexed { index, selectMenu ->
                when(selectMenu.type) {
                    SelectMenuType.USER,
                    SelectMenuType.ROLE,
                    SelectMenuType.CHANNEL -> {
                        selectMenu.options.mapIndexed { _index, option ->
                            if(option !is Snowflake)
                                throw IllegalArgumentException("Option $option is not of type ${Snowflake::class} on index $_index")
                        }
                    }
                    SelectMenuType.STRING   -> {
                        selectMenu.options.mapIndexed { _index, option ->
                            if(option !is SelectOptionBuilder)
                                throw IllegalArgumentException("Option $option is not of type ${SelectOptionBuilder::class} on index $_index")
                        }
                    }
                    else -> Unit
                }
                when(selectMenu.type) {
                    SelectMenuType.STRING       -> builder.components.add(createStringSelectMenu("$customId#$index",selectMenu))
                    SelectMenuType.CHANNEL      -> builder.components.add(createChannelSelectMenu("$customId#$index",selectMenu))
                    SelectMenuType.USER         -> builder.components.add(createUserSelectMenu("$customId#$index",selectMenu))
                    SelectMenuType.ROLE         -> builder.components.add(createRoleSelectMenu("$customId#$index",selectMenu))
                    SelectMenuType.MENTIONABLE  -> Unit
                }

                if (selectMenu.isOnce)
                    emitter.once(listenerBuilder(selectMenu,index))
                else
                    emitter.on(listenerBuilder(selectMenu,index))
            }
            return builder
        }


    }


}