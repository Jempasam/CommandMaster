package commandmaster.commands.arguments

import com.google.gson.JsonObject
import com.mojang.brigadier.CommandDispatcher
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
import net.minecraft.entity.Entity
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenTexts
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.function.FunctionLoader
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import java.util.concurrent.CompletableFuture

class MultiCommandArgumentType(val disp: CommandDispatcher<ServerCommandSource>?): ArgumentType<List<String>> {
    override fun parse(reader: StringReader): List<String> {
        // Parse text
        reader.expect('(')
        val commands=reader.readStringUntil(')').split(";").map { it.trim() }

        // Parse commands
        // val serverCommandSource = ServerCommandSource(CommandOutput.DUMMY, Vec3d.ZERO, Vec2f.ZERO, null as ServerWorld?, 0, "", ScreenTexts.EMPTY, null as MinecraftServer?, null as Entity?)
        // commands.forEach { disp?.parse(it, serverCommandSource)?.exceptions?.values?.first { throw it } }
        return commands
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
                val commands= stringReader.remaining
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
                else return source.getCompletions(context)
            }
        }
        return super.listSuggestions(context, builder)
    }

    object Properties: ArgumentTypeProperties<MultiCommandArgumentType>{
        override fun createType(commandRegistryAccess: CommandRegistryAccess)= MultiCommandArgumentType(null)
        override fun getSerializer() = Serializer
    }

    object Serializer: ArgumentSerializer<MultiCommandArgumentType, Properties>{
        override fun writePacket(properties: Properties, buf: PacketByteBuf) { }
        override fun fromPacket(buf: PacketByteBuf) = Properties
        override fun getArgumentTypeProperties(argumentType: MultiCommandArgumentType) = Properties
        override fun writeJson(properties: Properties, json: JsonObject) { }
    }
}