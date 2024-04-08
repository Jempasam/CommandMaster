package commandmaster.macro

import commandmaster.components.CmdMastComponents
import commandmaster.helper.overflow
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.world.World

object MacroUtils {
    fun getName(stack: ItemStack): MutableText {
        val macro=stack[CmdMastComponents.MACRO_HOLDER]
        val state=stack[CmdMastComponents.MACRO_COMPLETION]
        val macro_name= macro?.shortTextWith(state?: MacroCompletion()) ?: Text.translatable(stack.translationKey)
        val macro_max=macro?.parameters?.size ?: 0
        val state_max=state?.size ?: 0
        return macro_name.append(Text.of(" ($state_max/$macro_max)"))
    }

    fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val state=stack.get(CmdMastComponents.MACRO_COMPLETION) ?: MacroCompletion()
        val macro=stack.get(CmdMastComponents.MACRO_HOLDER)?.also { macro ->
            tooltip.add(macro.textWith(state).overflow(40,"...").styled{it.withItalic(false)})
        }
    }

    fun createSub(stack: ItemStack): ItemStack{
        val macro=stack[CmdMastComponents.MACRO_HOLDER]
        if(macro==null)return stack.item.defaultStack

        val ret=stack.item.defaultStack
        macro.sub(stack[CmdMastComponents.MACRO_COMPLETION] ?: MacroCompletion()).onSuccess {
            ret.set(CmdMastComponents.MACRO_HOLDER,it)
        }
        return ret
    }
}