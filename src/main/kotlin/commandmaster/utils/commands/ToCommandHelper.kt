package commandmaster.utils.commands

import net.minecraft.component.ComponentChanges
import net.minecraft.component.DataComponentType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtString
import net.minecraft.registry.Registries
import kotlin.jvm.optionals.getOrNull

fun ComponentChanges.toCommandArg(): String{
    val sb=StringBuilder()
    sb.append("[")
    fun<T> add(key: DataComponentType<T>){
        val id= Registries.DATA_COMPONENT_TYPE.getId(key) ?: return
        val value= this.get(key) ?: return
        if(value.isPresent){
            val nbt=key.codec?.encodeStart(NbtOps.INSTANCE,value.get())?.result()?.getOrNull() ?: return
            sb.append(id).append("=").append(if(nbt is NbtString) "'${nbt.asString()}'" else nbt.asString())
        }
        else{
            sb.append("!$id={}")
        }
        sb.append(",")
    }
    for((key,_) in this.entrySet())add(key)
    if(sb.length>1)sb.setLength(sb.length-1)
    sb.append("]")
    return sb.toString()
}

fun ItemStack.toCommandArg(): String{
    val id=Registries.ITEM.getId(this.item) ?: return ""
    return "$id${this.componentChanges.toCommandArg()}"
}