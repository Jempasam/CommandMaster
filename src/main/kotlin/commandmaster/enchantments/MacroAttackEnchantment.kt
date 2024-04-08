package commandmaster.enchantments

import commandmaster.components.CmdMastComponents
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroCompletion
import commandmaster.macro.MacroParamType
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity

class MacroAttackEnchantment(properties: Properties): Enchantment(properties) {

    private var lasttime=-1L

    override fun onTargetDamaged(user: LivingEntity, target: Entity, level: Int) {
        if(user.world.time==lasttime) return
        else lasttime=user.world.time
        val server=user.server ?: return
        val stack=user.getStackInHand(user.activeHand)
        val macro=stack.get(CmdMastComponents.MACRO_HOLDER) ?: return
        val builder=MacroCompletion.builder()
        runCatching{
            macro.parameters.forEach { arg -> builder.add(target,arg) }
            val command=macro.build(builder.build()).getOrThrow()
            MacroCommand.executeMultiline(server,user.commandSource.withMaxLevel(2).withSilent(),command)
        }
    }

    override fun isAvailableForEnchantedBookOffer() = false
    override fun isAvailableForRandomSelection() = false
    override fun isTreasure() = true
}