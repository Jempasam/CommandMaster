package commandmaster.helper

import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.util.Optional
import kotlin.math.max
import kotlin.math.min

fun String.overflow(size: Int, suffix: String=""): String{
    return if(this.length>size) this.substring(0,size)+suffix else this
}
fun Text.overflow(size: Int, suffix: String=""): MutableText{
    val ret=Text.empty()
    var size=size
    this.visit({ style, str ->
        if(size>0){
            val cutted= min(str.length,size)
            ret.append(Text.literal(str.take(cutted)).setStyle(style))
            size-=cutted
            if(size<=0)ret.append(Text.literal("..."))
            Optional.empty()
        }
        else Optional.of(0)
    }, Style.EMPTY)
    return ret
}