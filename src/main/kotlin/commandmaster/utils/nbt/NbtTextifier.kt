package commandmaster.utils.nbt

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Formatting.*


private val STYLES= arrayOf(RED, GOLD, YELLOW, GREEN, BLUE)

fun NbtCompound.toText(level: Int=0): MutableText{
    if(this.isEmpty)return Text.literal("{}")
    val indent="  ".repeat(level)
    val ret= Text.literal("{\n").styled { it.withColor(STYLES[level%STYLES.size]) }

    for(key in this.keys) {
        ret.append(indent + "  $key: ").append(this.get(key)?.toText(level + 1)).append(Text.literal("\n"))
    }

    ret.append(indent+"}")
    return ret
}

fun NbtList.toText(level: Int=0): MutableText{
    if(this.isEmpty())return Text.literal("[]")
    val indent="  ".repeat(level)
    val ret= Text.literal("[\n").styled { it.withColor(STYLES[level%STYLES.size]) }

    for(value in this) {
        ret.append(indent + "  ").append(value.toText(level + 1)).append(Text.literal("\n"))
    }

    ret.append(indent+"]")
    return ret
}

fun NbtElement.toText(level: Int=0): MutableText{
    return when(this){
        is NbtCompound -> this.toText(level)
        is NbtList -> this.toText(level)
        else -> Text.literal(this.toString()).styled { it.withColor(STYLES[level%STYLES.size]) }
    }
}