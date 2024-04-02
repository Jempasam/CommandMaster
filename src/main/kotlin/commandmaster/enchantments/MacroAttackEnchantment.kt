package commandmaster.enchantments

import commandmaster.components.CmdMastComponents
import commandmaster.macro.MacroCommand
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
        macro.map { type, _ -> type.selectEntity(user,target) } // Params
            .let { macro.build(it) }
            ?.let { MacroCommand.executeMultiline(server,user.commandSource.withMaxLevel(2).withSilent(),it) }
    }

    override fun isAvailableForEnchantedBookOffer() = false
    override fun isAvailableForRandomSelection() = false
    override fun isTreasure() = true
}