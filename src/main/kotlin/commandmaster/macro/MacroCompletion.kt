package commandmaster.macro

import com.mojang.serialization.Codec
import commandmaster.commands.arguments.MacroCommandArgumentType
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import javax.crypto.Mac
import kotlin.reflect.KProperty1

// A state of completion of a macro command
class MacroCompletion private constructor(val parameters: List<Arg> = listOf()){
    @JvmInline
    value class Arg(val map: Map<MacroParamType, String>){
        fun get(type: MacroParamType) = map[type] ?: throw IncompatibleCompletion

        override fun toString() = map.entries.map { (key, value)->"${key.name}=$value" }.joinToString(", ").let { "{$it}" }
    }

    class InvalidTarget(val type: MacroParamType): Exception(){
        override val message get() = "Invalid target for type ${type.name}"
    }

    object IncompatibleCompletion: Exception("No such type in completion")

    object IncompleteCompletion: Exception("Incomplete completion")


    constructor(): this(listOf())

    /**
     * The number of parameter completed in the completion
     */
    val size = parameters.size

    /**
     * Get the value of a parameter in the completion for a perculiar type
     * @throws IncompatibleCompletion if the type is not in the completion
     */
    fun get(count: Int, type: MacroParamType) = parameters[count].get(type)

    fun builder() = Builder(this)

    inline fun modify(action: Builder.()->Unit): MacroCompletion{
        val builder=builder()
        builder.action()
        return builder.build()
    }

    override fun toString() = parameters.toString()

    companion object {
        val CODEC= Codec.unboundedMap(Codec.STRING, Codec.STRING) .listOf() .xmap(
            { encoded->
                val ret= mutableListOf<Arg>()
                for(parameter in encoded){
                    val map= mutableMapOf<MacroParamType, String>()
                    for((key, value) in parameter){
                        val type=MacroParamType.TYPES[key] ?: continue
                        map[type]=value
                    }
                    ret+=Arg(map)
                }
                MacroCompletion(ret)
            },
            { decoded->
                val encoded= mutableListOf<Map<String,String>>()
                for(parameter in decoded.parameters){
                    val map= mutableMapOf<String, String>()
                    for((key, value) in parameter.map){
                        val type=MacroParamType.TYPES.getKey(key) ?: continue
                        map[type]=value
                    }
                    encoded+=map
                }
                encoded
            }
        )

        fun builder()= Builder()
    }

    class Builder private constructor(val parameters: MutableList<Arg>){
        constructor(): this(mutableListOf())

        constructor(base: MacroCompletion): this(base.parameters.toMutableList())

        fun<T> add(target: T, prop: MacroParamType.(T)->String?, arg: MacroCommand.Arg): Builder{
            val values= mutableMapOf<MacroParamType,String>()
            for(type in arg.map){
                val value=type.prop(target)
                if(value===null)throw InvalidTarget(type)
                values[type]=value
            }
            parameters+=Arg(values)
            return this
        }

        fun add(target: ItemStack, arg: MacroCommand.Arg) = add(target, MacroParamType::of, arg)
        fun add(target: Entity, arg: MacroCommand.Arg) = add(target, MacroParamType::of, arg)
        fun add(block: CachedBlockPosition, arg: MacroCommand.Arg) = add(block, MacroParamType::of, arg)
        fun add(value: String, arg: MacroCommand.Arg) = add(value, MacroParamType::of, arg)
        fun add(value: NbtElement, arg: MacroCommand.Arg) = add(value, MacroParamType::of, arg)

        fun<T> add(target: T, prop: MacroParamType.(T)->String?, arg: MacroCommand): Builder{
            return add(target,prop,arg.parameters[parameters.size])
        }

        fun add(target: ItemStack, arg: MacroCommand) = add(target, MacroParamType::of, arg)
        fun add(target: Entity, arg: MacroCommand) = add(target, MacroParamType::of, arg)
        fun add(block: CachedBlockPosition, arg: MacroCommand) = add(block, MacroParamType::of, arg)
        fun add(value: String, arg: MacroCommand) = add(value, MacroParamType::of, arg)
        fun add(value: NbtElement, arg: MacroCommand) = add(value, MacroParamType::of, arg)

        fun build() = MacroCompletion(parameters)
    }
}

inline fun macroCompletion(block: MacroCompletion.Builder.()->Unit) = MacroCompletion.builder().apply(block).build()