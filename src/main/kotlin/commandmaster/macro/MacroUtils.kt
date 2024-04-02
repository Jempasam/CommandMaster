package commandmaster.macro

import commandmaster.components.CmdMastComponents
import commandmaster.helper.overflow
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.world.World
import java.util.function.Consumer

object MacroUtils {
    fun getName(stack: ItemStack): MutableText {
        val macro=stack[CmdMastComponents.MACRO_HOLDER]
        val state=stack[CmdMastComponents.MACRO_STATE]
        val macro_name= macro?.shortTextWith(state?: listOf()) ?: Text.translatable(stack.translationKey)
        val macro_max=macro?.parameters?.size ?: 0
        val state_max=state?.size ?: 0
        return macro_name.append(Text.of(" ($state_max/$macro_max)"))
    }

    fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val state=stack.get(CmdMastComponents.MACRO_STATE) ?: listOf()
        val macro=stack.get(CmdMastComponents.MACRO_HOLDER)?.also { macro ->
            tooltip.add(macro.textWith(state).overflow(40,"...").styled{it.withItalic(false)})
        }
    }

    fun createSub(stack: ItemStack): ItemStack{
        val macro=stack[CmdMastComponents.MACRO_HOLDER]
        if(macro==null)return stack.item.defaultStack

        val ret=stack.item.defaultStack
        ret.set(CmdMastComponents.MACRO_HOLDER,macro.sub(stack[CmdMastComponents.MACRO_STATE] ?: listOf()))
        return ret
    }
}