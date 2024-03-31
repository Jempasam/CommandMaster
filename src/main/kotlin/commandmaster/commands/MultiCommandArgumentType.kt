package commandmaster.commands

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.command.argument.serialize.ArgumentSerializer.ArgumentTypeProperties
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object MultiCommandArgumentType: ArgumentType<List<String>> {
    override fun parse(reader: StringReader): List<String> {
        reader.expect('(')
        val commands=reader.readStringUntil(')')
        return commands.split(";").map { it.trim() }
    }

    override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val source=context.source
        run testgroup@{
            if(source is CommandSource){
                // String reader
                val stringReader = StringReader(builder.input)
                stringReader.cursor = builder.start
                stringReader.skip()

                // Get commands
                if(stringReader.remainingLength<=0)return@testgroup
                val commands=stringReader.remaining
                if(commands.isNotEmpty() && commands.last()==')')return@testgroup
                var last_index=commands.lastIndexOf(';')
                if(last_index==-1)last_index=0
                else last_index++
                val last_command=commands.substring(last_index)

                // Get completions
                if(source is ServerCommandSource){
                    source.server.commandManager.dispatcher.let { dispatcher ->
                        val parsed=dispatcher.parse(last_command, source)
                        val suggestions=dispatcher.getCompletionSuggestions(parsed)
                        return suggestions.thenApply {sugs ->
                            Suggestions.create(
                                context.input,
                                sugs.list.map { Suggestion(StringRange(builder.start+last_index+it.range.start+1, builder.start+commands.length+1),it.text) }
                            )
                        }
                    }
                }
                else{
                    println("Not a server command source")
                    return source.getCompletions(context)
                }

            }
        }
        return super.listSuggestions(context, builder)
    }

    object Properties: ArgumentTypeProperties<MultiCommandArgumentType>{
        override fun createType(commandRegistryAccess: CommandRegistryAccess)= MultiCommandArgumentType
        override fun getSerializer() = Serializer
    }

    object Serializer: ArgumentSerializer<MultiCommandArgumentType,Properties>{
        override fun writePacket(properties: Properties, buf: PacketByteBuf) { }
        override fun fromPacket(buf: PacketByteBuf) = Properties
        override fun getArgumentTypeProperties(argumentType: MultiCommandArgumentType) = Properties
        override fun writeJson(properties: Properties, json: JsonObject) { }
    }
}