package commandmaster.commands

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroParamType
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.command.argument.serialize.ArgumentSerializer.ArgumentTypeProperties
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object MacroCommandArgumentType: ArgumentType<String> {
    override fun parse(reader: StringReader): String{
        val ret=reader.remaining
        reader.cursor=reader.totalLength

        var afterarg=false
        ret.forEach {
            if(afterarg){
                MacroParamType.TYPES[it] ?: throw IllegalArgumentException("Invalid macro argument type: \"$it\"")
                afterarg=false
            }
            else if(it=='$')afterarg=true
        }
        if(afterarg)throw IllegalArgumentException("Expected macro argument type after macro argument symbol \"$\"")
        return ret
    }

    override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val source=context.source
        run testgroup@{
            if(source is CommandSource){
                // String reader
                val stringReader = StringReader(builder.input)
                stringReader.cursor = builder.start

                // Get commands
                if(stringReader.remainingLength<=0)return@testgroup
                val command=stringReader.remaining

                // Last is arg
                if(command.last()=='$'){
                    return CompletableFuture.supplyAsync {
                        val builder=builder.createOffset(builder.start+command.length-1)
                        builder.createOffset(command.length-1)
                        for((key,arg) in MacroParamType.TYPES)builder.suggest("\$$key")
                        builder.build()
                    }
                }

                // Get completions
                if(source is ServerCommandSource){
                    // Complete args
                    val macro=MacroCommand(command)
                    var result=""
                    macro.visit(
                        {part -> result+=part},
                        {param -> result+=param.example}
                    )

                    // Suggest
                    source.server.commandManager.dispatcher.let { dispatcher ->
                        val parsed=dispatcher.parse(result, source)
                        val suggestions=dispatcher.getCompletionSuggestions(parsed)
                        val offset=command.length-result.length
                        return suggestions.thenApply {sugs ->
                            Suggestions.create(
                                context.input,
                                sugs.list.map { Suggestion(StringRange(builder.start+it.range.start+offset, builder.start+it.range.end+offset),it.text) }
                            )
                        }
                    }
                }
                else return source.getCompletions(context)

            }
        }
        return super.listSuggestions(context, builder)
    }

    object Properties: ArgumentTypeProperties<MacroCommandArgumentType>{
        override fun createType(commandRegistryAccess: CommandRegistryAccess)= MacroCommandArgumentType
        override fun getSerializer() = Serializer
    }

    object Serializer: ArgumentSerializer<MacroCommandArgumentType,Properties>{
        override fun writePacket(properties: Properties, buf: PacketByteBuf) { }
        override fun fromPacket(buf: PacketByteBuf) = Properties
        override fun getArgumentTypeProperties(argumentType: MacroCommandArgumentType) = Properties
        override fun writeJson(properties: Properties, json: JsonObject) { }
    }
}