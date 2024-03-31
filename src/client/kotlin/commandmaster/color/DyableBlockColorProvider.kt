package commandmaster.color

import commandmaster.blockentity.getComponentBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.client.color.block.BlockColorProvider
import net.minecraft.client.color.item.ItemColorProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.item.ItemStack
import net.minecraft.util.Colors
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ColorHelper.Argb
import net.minecraft.world.BlockRenderView

object DyableBlockColorProvider: BlockColorProvider {
    override fun getColor(state: BlockState, world: BlockRenderView?, pos: BlockPos?, tintIndex: Int): Int {
        if(pos!=null && world!=null && tintIndex==0) {
            world.getComponentBlockEntity(pos)
                ?.get(DataComponentTypes.DYED_COLOR)
                ?.rgb
                ?.let { return Argb.fullAlpha(it) }
        }
        return Argb.fullAlpha(Colors.WHITE)
    }
}