package commandmaster.commands.arguments

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
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
import net.minecraft.enchantment.Enchantments
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object MacroCommandArgumentType: ArgumentType<String> {
    override fun parse(reader: StringReader): String{
        val ret=reader.remaining
        reader.cursor=reader.totalLength

        var pos=0
        var word=""
        ret.forEach {
            if(pos==3){
                MacroParamType.TYPES[word.take(1)] ?: MacroParamType.TYPES[word]?: throw IllegalArgumentException("Invalid macro argument type: \"$it\"")
                word=""
                pos=0
            }
            else if(pos>0){
                pos++
                word+=it
            }
            else if(it=='$') pos=1
        }
        if(pos==1)throw IllegalArgumentException("Expected macro argument type after macro argument symbol \"$\"")
        else if(pos==2)MacroParamType.TYPES[word] ?: throw IllegalArgumentException("Invalid macro argument type: \"$word\"")

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
                val command=stringReader.remaining

                // Last is arg
                if(command.isNotEmpty() && command.last()=='$'){
                    return CompletableFuture.supplyAsync {
                        val builder=builder.createOffset(builder.start+command.length-1)
                        builder.createOffset(command.length-1)
                        for((key,arg) in MacroParamType.TYPES)builder.suggest("\$$key")
                        builder.build()
                    }
                }
                Enchantments.THORNS

                // Get completions
                if(source is ServerCommandSource){
                    // Complete args
                    val macro=MacroCommand(command)
                    val result=macro.example

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

    override fun getExamples() = listOf("setblock \$p dirt", "fill \$p \$p \$b", "give \$i")

    object Properties: ArgumentTypeProperties<MacroCommandArgumentType>{
        override fun createType(commandRegistryAccess: CommandRegistryAccess)= MacroCommandArgumentType
        override fun getSerializer() = Serializer
    }

    object Serializer: ArgumentSerializer<MacroCommandArgumentType, Properties>{
        override fun writePacket(properties: Properties, buf: PacketByteBuf) { }
        override fun fromPacket(buf: PacketByteBuf) = Properties
        override fun getArgumentTypeProperties(argumentType: MacroCommandArgumentType) = Properties
        override fun writeJson(properties: Properties, json: JsonObject) { }
    }
}