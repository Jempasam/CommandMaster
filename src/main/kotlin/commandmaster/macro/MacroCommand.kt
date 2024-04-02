package commandmaster.macro

import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import commandmaster.codec.getWith
import commandmaster.codec.of
import com.mojang.serialization.Codec.*
import com.mojang.serialization.codecs.RecordCodecBuilder
import commandmaster.helper.overflow
import io.netty.handler.codec.CodecException
import net.minecraft.client.item.TooltipContext
import net.minecraft.command.CommandSource
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.item.TooltipAppender
import net.minecraft.registry.Registries
import net.minecraft.screen.AnvilScreenHandler
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.util.function.Consumer
import kotlin.math.min

/**
 * Represent a macro command.
 * Per example: "setblock $p minecraft:dirt" mean "set a block of dirt at a parametrized position"
 */
data class MacroCommand(val command: String): TooltipAppender {

    val parts: List<String>
    val parameters: List<MacroParamType>

    init{
        val fparts= mutableListOf<String>()
        val fparameters= mutableListOf<MacroParamType>()

        val splitted=command.split('$').asSequence().iterator()
        if(splitted.hasNext())fparts.add(splitted.next())
        for(part in splitted){
            if(part.length>0){
                val type=MacroParamType.TYPES[part[0]]
                if(type!=null) {
                    fparameters.add(type)
                    fparts.add(part.substring(1))
                }
                else{
                    fparts.set(fparts.size-1,"${fparts.last()}$${part}")
                }
            }
        }
        parts=fparts
        parameters=fparameters
    }

    inline fun visit(on_part: (String)->Unit, on_param: (MacroParamType)->Unit){
        on_part(parts[0])
        for(i in 0 until parameters.size){
            on_param(parameters[i])
            on_part(parts[i+1])
        }
    }

    inline fun visit(values: List<String>, on_part: (String)->Unit, on_value: (String,MacroParamType)->Unit, on_param: (MacroParamType)->Unit){
        on_part(parts[0])
        var i=0
        val min= min(parameters.size,values.size)
        while(i<min){
            on_value(values[i],parameters[i])
            on_part(parts[i+1])
            i++
        }
        while(i<parameters.size){
            on_param(parameters[i])
            on_part(parts[i+1])
            i++
        }
    }

    inline fun<T> map(mapper: (MacroParamType,Int)->T): MutableList<T>{
        val ret= mutableListOf<T>()
        for(i in parameters.indices){
            val type=parameters[i]
            ret.add(mapper(type,i))
        }
        return ret
    }

    fun build(params: List<String>): String?{
        var result=""
        visit( params,
            {part-> result+=part},
            {value,type-> result+=value},
            {param-> return null}
        )
        return result
    }

    fun sub(params:List<String>): MacroCommand{
        var result=""
        visit( params,
            {part-> result+=part},
            {value,type-> result+=value},
            {param->
                val letter=MacroParamType.TYPES.getKey(param)
                result+="$$letter"
            }
        )
        return MacroCommand(result)
    }

    fun textWith(params: List<String>): Text{
        val result= Text.empty()
        visit(params,
            {part-> result.append(Text.literal(part))},
            {value,type-> result.append(Text.literal(value).withColor(type.color))},
            {param-> result.append(Text.literal("$${param.name}").withColor(param.color))}
        )
        return result
    }

    fun shortTextWith(params: List<String>): MutableText{
        val result= Text.empty()
        visit(params,
            {part-> result.append(Text.literal(part))},
            {value,type-> result.append(Text.literal(value).withColor(type.color))},
            {param-> result.append(Text.literal("$").withColor(param.color))}
        )
        return result
    }

    val text: MutableText get(){
        val result= Text.empty()
        visit(
            {part-> result.append(Text.literal(part))},
            {param-> result.append(Text.literal("$${param.name}").withColor(param.color))}
        )
        return result
    }

    override fun appendTooltip(textConsumer: Consumer<Text>, context: TooltipContext) {
        if(context.isAdvanced){
            textConsumer.accept(text.overflow(40,"...").styled{it.withItalic(false)})
            textConsumer.accept(Text.of(command.overflow(40,"...")))
        }
    }


    override fun toString() = "Macro{$command}"

    companion object{
        val OLD_CODEC: Codec<MacroCommand> = RecordCodecBuilder.create {
            it.group(
                "command" of STRING getWith MacroCommand::command
            ).apply(it, ::MacroCommand)
        }
        val CODEC = STRING.xmap({MacroCommand(it)},{it.command})

        fun executeMultiline(server: MinecraftServer, context: ServerCommandSource, command: String){
            server.commandManager.executeWithPrefix(context, command)
        }
    }
}

fun main() {
    val command=MacroCommand("setblock \$p \$p minecraft:dirt")
    println(command.build(listOf("1 2 3")))
    println(command.build(listOf("1 2 3","4 5 6")))
    println(command.sub(listOf("1 2 3")))
    println(command)
}