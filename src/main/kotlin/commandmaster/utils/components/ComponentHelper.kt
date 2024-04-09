package commandmaster.utils.components

import net.minecraft.component.Component
import net.minecraft.component.DataComponentType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

fun<T: Any> Optional<T>.mergeFrom(type: DataComponentType<T>, other: Optional<T>): Optional<T> {
    if(other.isEmpty || this.isEmpty)return other
    val thisnbt= type.codec?.encodeStart(NbtOps.INSTANCE,this.get())?.result()?.getOrNull() ?: return other
    val othernbt= type.codec?.encodeStart(NbtOps.INSTANCE,other.get())?.result()?.getOrNull() ?: return other
    var merged: NbtElement?=null
    if(thisnbt is NbtList && othernbt is NbtList){
        val m= NbtList()
        thisnbt.forEach{m.add(it)}
        othernbt.forEach{m.add(it)}
        merged=m
    }
    else if(thisnbt is NbtCompound && othernbt is NbtCompound){
        thisnbt.copyFrom(othernbt)
        merged=thisnbt
    }
    merged ?:return other
    val mergedValue=type.codec?.decode(NbtOps.INSTANCE,merged)?.result()?.getOrNull()?.first ?: return other
    return Optional.of<T>(mergedValue)
}