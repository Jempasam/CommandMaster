package commandmaster.macro

import com.mojang.serialization.Codec
import commandmaster.codec.getWith
import commandmaster.codec.of
import com.mojang.serialization.Codec.*
import com.mojang.serialization.codecs.RecordCodecBuilder
import commandmaster.commands.arguments.MacroCommandArgumentType
import commandmaster.helper.overflow
import commandmaster.utils.builders.nbt
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.TooltipAppender
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

/**
 * Represent a macro command.
 * Per example: "setblock $p minecraft:dirt" mean "set a block of dirt at a parametrized position"
 */
data class MacroCommand(val command: String): TooltipAppender {

    class Arg(val map: Set<MacroParamType>)

    val parameters: List<Arg>
    val str_parts: List<String>
    val param_parts: List<Pair<MacroParamType,Int>>

    init{
        // Parameters
        val parameters= mutableListOf<MutableSet<MacroParamType>>()
        fun set(index: Int, type: MacroParamType){
            while(parameters.size<=index)parameters.add(mutableSetOf())
            parameters[index].add(type)
        }

        // Macro parts
        val str_parts= mutableListOf<String>()
        val param_parts= mutableListOf<Pair<MacroParamType,Int>>()


        val splitted=command.split('$').asSequence().iterator()
        if(splitted.hasNext())str_parts.add(splitted.next())
        var nextArgNum=0
        for(part in splitted){
            var i=0
            var argnum=nextArgNum
            var type: MacroParamType?=null

            // Get num
            if(part[i] in '0'..'9') {
                argnum=part[i]-'0'
                i++
            }

            // Get type
            if(part.length>i+1){
                type = MacroParamType.TYPES[part.substring(i, i+2)]
                if(type!=null)i+=2
            }
            if(type==null && part.length>i){
                type = MacroParamType.TYPES[part.substring(i, i+1)]
                if(type!=null)i+=1
            }

            // If it is a valid parameter, add it
            if(type!=null){
                // Add to the parameter list
                set(argnum, type)

                // Add to the parts
                param_parts+= type to argnum
                str_parts+= part.substring(i)

                nextArgNum=argnum
            }
            else{
                str_parts.set(str_parts.size-1, "${str_parts.last()}$${part}")
            }

            nextArgNum= max(nextArgNum, argnum+1)
        }

        this.parameters=parameters.map{Arg(it)}
        this.str_parts=str_parts
        this.param_parts=param_parts
    }

    inline fun visit(on_part: (String)->Unit, on_param: (MacroParamType, Int)->Unit){
        on_part(str_parts[0])
        for(i in param_parts.indices){
            on_param(param_parts[i].first, param_parts[i].second)
            on_part(str_parts[i+1])
        }
    }

    val INVALID="[INVALID]"
    inline fun visit(completion: MacroCompletion, on_part: (String)->Unit, on_value: (String,MacroParamType,Int)->Unit, on_param: (MacroParamType,Int)->Unit){
        visit(
            {part-> on_part(part) },
            {param, num->
                if(num<completion.size){
                    try{
                        val value=completion.get(num, param)
                        on_value(value, param, num)
                    }catch (e: MacroCompletion.IncompatibleCompletion){
                        on_value(INVALID, param, num)
                    }
                }
                else on_param(param, num)
            }
        )
    }

    inline fun<T> map(mapper: (MacroParamType, Int, Int)->T): MutableList<T>{
        val ret= mutableListOf<T>()
        for(i in param_parts.indices){
            val type=param_parts[i]
            ret.add(mapper(type.first, type.second, i))
        }
        return ret
    }

    fun build(params: MacroCompletion): Result<String>{
        var result=""
        visit( params,
            {part-> result+=part},
            {value,_, _->
                if(value===INVALID) return Result.failure(MacroCompletion.IncompatibleCompletion)
                else result+=value
            },
            {_, _-> return Result.failure(MacroCompletion.IncompleteCompletion)}
        )
        return Result.success(result)
    }

    fun sub(params: MacroCompletion): Result<MacroCommand>{
        var result=""
        visit( params,
            {part-> result+=part},
            {value,_, _->
                if(value===INVALID) return Result.failure(MacroCompletion.IncompatibleCompletion)
                else result+=value
            },
            {param, num->
                val letter=MacroParamType.TYPES.getKey(param)
                result+="$${num-params.size}$letter"
            }
        )
        return Result.success(MacroCommand(result))
    }

    fun textWith(params: MacroCompletion): Text{
        val result= Text.empty()
        visit(params,
            {part-> result.append(Text.literal(part))},
            {value,type, _-> result.append(Text.literal(value).withColor(type.color))},
            {param, pos-> result.append(Text.literal("$$pos${param.name}").withColor(param.color))}
        )
        return result
    }

    fun shortTextWith(params: MacroCompletion): MutableText{
        val result= Text.empty()
        visit(params,
            {part-> result.append(Text.literal(part))},
            {value,type,_-> result.append(Text.literal(value).withColor(type.color))},
            {param,_-> result.append(Text.literal("$").withColor(param.color))}
        )
        return result
    }

    val colored_command: MutableText get(){
        val result= Text.empty()
        visit(
            {part-> result.append(Text.literal(part))},
            {param,pos-> result.append(Text.literal("$$pos${MacroParamType.TYPES.getKey(param)}").withColor(param.color))}
        )
        return result
    }

    val text: MutableText get(){
        val result= Text.empty()
        visit(
            {part-> result.append(Text.literal(part))},
            {param,pos-> result.append(Text.literal("$$pos${param.name}").withColor(param.color))}
        )
        return result
    }

    val example: String get(){
        val result= StringBuilder()
        visit(
            {part-> result.append(part)},
            {param,_-> result.append(param.example)}
        )
        return result.toString()
    }

    override fun appendTooltip(textConsumer: Consumer<Text>, context: TooltipContext) {
        if(context.isAdvanced){
            //textConsumer.accept(text.overflow(40,"...").styled{it.withItalic(false)})
            textConsumer.accept(colored_command.overflow(40,"..."))
        }
    }


    override fun toString() = "Macro{$command}"

    companion object{
        val CODEC = STRING.xmap({MacroCommand(it)},{it.command})

        fun executeMultiline(server: MinecraftServer, context: ServerCommandSource, command: String){
            server.commandManager.executeWithPrefix(context, command)
        }
    }
}

fun main() {
    val command=MacroCommand("setblock \$p \$p minecraft:dirt")
    command.visit(
        {part-> print(part)},
        {param, num-> print("{${param.name},$num}")}
    )
    println()
    run{
        val completion= macroCompletion {
            add(nbt(10,5,5),command)
            add(nbt("z" to 20.nbt, "y" to 10.nbt, "x" to 5.nbt),command)
        }
        println(completion)
        println(command.sub(completion).map { it.command })
    }
    /*println(command.build())
    println(command.build(listOf("1 2 3","4 5 6")))
    println(command.sub(listOf("1 2 3")))*/
    println(command)
}