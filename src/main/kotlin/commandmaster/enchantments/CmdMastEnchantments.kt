package commandmaster.enchantments

import commandmaster.CommandMaster
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EquipmentSlot
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.ItemTags

object CmdMastEnchantments {

    val MACRO_ATTACK=register("macro_attack", MacroAttackEnchantment(Enchantment.properties(
        ItemTags.WEAPON_ENCHANTABLE, 10, 1,
        Enchantment.leveledCost(1, 10),
        Enchantment.leveledCost(12, 11),
        1, EquipmentSlot.MAINHAND
    )))

    val MACRO_THORNS=register("macro_thorns", MacroThornEnchantment(Enchantment.properties(
        ItemTags.ARMOR_ENCHANTABLE, 10, 1,
        Enchantment.leveledCost(1, 10),
        Enchantment.leveledCost(12, 11),
        1, EquipmentSlot.HEAD, EquipmentSlot.BODY, EquipmentSlot.LEGS, EquipmentSlot.FEET
    )))

    fun<T: Enchantment> register(id: String, enchantment: T): T{
        Registry.register(Registries.ENCHANTMENT, CommandMaster/id, enchantment)
        return enchantment
    }
}