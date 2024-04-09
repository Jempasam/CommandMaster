package commandmaster.utils.builders

import commandmaster.CommandMaster
import commandmaster.components.CmdMastComponents
import commandmaster.components.UpgraderComponent
import commandmaster.macro.MacroCommand
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
import net.minecraft.component.Component
import net.minecraft.component.ComponentChanges
import net.minecraft.component.DataComponentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.*
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.ColorHelper
import java.util.Optional

class ComponentsBuilder(val stack: Target){

    interface Target{
        fun<T> set(type: DataComponentType<T>, value: T)
    }

    class ItemStackTarget(val stack: ItemStack): Target{
        override fun<T> set(type: DataComponentType<T>, value: T){ stack.set(type, value) }
    }

    class ChangesTarget(val builder: ComponentChanges.Builder): Target{
        override fun<T> set(type: DataComponentType<T>, value: T){ builder.add(type, value) }
    }

    fun name(text: Text) = stack.set(DataComponentTypes.CUSTOM_NAME, text)
    fun name(id: String) = name(CommandMaster.translatable("example", id).styled{it.withItalic(false)})
    fun lore(vararg lore: Text) = stack.set(DataComponentTypes.LORE, LoreComponent(lore.toList()))
    fun lore(vararg lore: String) = lore(*lore.map{ CommandMaster.translatable("example",it).styled{it.withItalic(false).withColor(Formatting.GRAY)}}.toTypedArray())
    fun color(color: Int) = stack.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(color,false))
    fun color(red: Int, green: Int, blue: Int) = color(ColorHelper.Argb.getArgb(red,green,blue))
    fun macro(macro: String) = stack.set(CmdMastComponents.MACRO_HOLDER, MacroCommand(macro))
    fun ench(vararg enchs: Pair<Enchantment,Int>){
        val map=ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT)
        for((ench, level) in enchs)map.set(ench, level)
        stack.set(DataComponentTypes.ENCHANTMENTS, map.build())
    }
    fun<T> add(type: DataComponentType<T>, value: T) = stack.set(type, value)
    fun edible(hunger: Int=2) = add(DataComponentTypes.FOOD, FoodComponent.Builder().hunger(hunger).saturationModifier(0.6f).build())
    fun model(data: Int) = stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent(data))

    fun giving(target: TagKey<Item>?=null, merge: Boolean=false, show: Boolean=true, levelCost: Int=1, builder: ComponentsBuilder.()->Unit) {
        val subbuilder=ComponentChanges.builder()
        Items.COOKED_COD
        ComponentsBuilder(ChangesTarget(subbuilder)).builder()
        stack.set(CmdMastComponents.UPGRADER_COMPONENT, UpgraderComponent(Optional.ofNullable(target), subbuilder.build(),levelCost,show,merge))
    }

    fun entity(entity: NbtCompound) = stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(entity))

    fun entity(vararg pairs: Pair<String, NbtElement>) = entity(nbt(*pairs))

    companion object{
        fun stack(target: ItemStack) = ComponentsBuilder(ItemStackTarget(target))

        fun changes(target: ComponentChanges.Builder) = ComponentsBuilder(ChangesTarget(target))
    }
}