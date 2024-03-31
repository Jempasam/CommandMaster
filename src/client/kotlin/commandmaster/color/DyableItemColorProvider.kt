package commandmaster.color

import net.minecraft.client.color.item.ItemColorProvider
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.item.ItemStack
import net.minecraft.util.Colors
import net.minecraft.util.math.ColorHelper.Argb

class DyableItemColorProvider(val default: Int): ItemColorProvider {
    override fun getColor(stack: ItemStack, tintIndex: Int)
        = if(tintIndex==0) DyedColorComponent.getColor(stack, Argb.fullAlpha(default)) else Argb.fullAlpha(Colors.WHITE)

}