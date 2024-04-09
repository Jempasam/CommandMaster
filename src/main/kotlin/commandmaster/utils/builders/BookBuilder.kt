package commandmaster.utils.builders

import net.minecraft.text.RawFilteredPair
import net.minecraft.text.Text

@JvmInline
value class BookBuilder(val pages: MutableList<RawFilteredPair<Text>>) {
    operator fun Text.unaryPlus(){
        pages.add(RawFilteredPair.of(this))
    }

    operator fun String.unaryPlus() = +Text.of(this)
}