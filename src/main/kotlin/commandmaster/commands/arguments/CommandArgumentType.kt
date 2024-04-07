package commandmaster.commands.arguments

import com.google.gson.JsonObject
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
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

class CommandArgumentType(val dispatcher: CommandDispatcher<ServerCommandSource>?): ArgumentType<ParseResults<ServerCommandSource>?> {
    override fun parse(reader: StringReader): ParseResults<ServerCommandSource>?{
        val ret=reader.remaining
        reader.cursor=reader.totalLength
        if(dispatcher!=null)return dispatcher.parse(ret, ArgumentTypeHelper.NULL_COMMAND_SOURCE)
        else return null
    }

    override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val source=context.source
        run testgroup@{
            if(source is ServerCommandSource){
                // String reader
                val stringReader = StringReader(builder.input)
                stringReader.cursor = builder.start

                // Get commands
                if(stringReader.remainingLength<=0)return@testgroup
                val command=stringReader.remaining

                // Suggest
                source.server.commandManager.dispatcher.let { dispatcher ->
                    val parsed=dispatcher.parse(command, source)
                    val suggestions=dispatcher.getCompletionSuggestions(parsed)
                    return suggestions.thenApply {sugs ->
                        Suggestions.create(
                            context.input,
                            sugs.list.map { Suggestion(StringRange(builder.start+it.range.start, builder.start+it.range.end),it.text) }
                        )
                    }
                }
            }
            else if(source is CommandSource) return source.getCompletions(context)
        }
        return super.listSuggestions(context, builder)
    }

    override fun getExamples() = listOf("setblock \$p dirt", "fill \$p \$p \$b", "give \$i")

    object Properties: ArgumentTypeProperties<CommandArgumentType>{
        override fun createType(commandRegistryAccess: CommandRegistryAccess)= CommandArgumentType(null)
        override fun getSerializer() = Serializer
    }

    object Serializer: ArgumentSerializer<CommandArgumentType, Properties>{
        override fun writePacket(properties: Properties, buf: PacketByteBuf) { }
        override fun fromPacket(buf: PacketByteBuf) = Properties
        override fun getArgumentTypeProperties(argumentType: CommandArgumentType) = Properties
        override fun writeJson(properties: Properties, json: JsonObject) { }
    }
}