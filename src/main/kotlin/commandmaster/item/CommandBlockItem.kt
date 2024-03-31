package commandmaster.item

import commandmaster.macro.MacroUtils
import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

class CommandBlockItem(block: Block, settings: Settings) : BlockItem(block, settings){

    override fun getName(stack: ItemStack) = MacroUtils.getName(stack).append(" ").append(super.getName(stack))

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        MacroUtils.appendTooltip(stack,world,tooltip,context)
    }

}