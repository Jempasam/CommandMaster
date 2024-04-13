package commandmaster.utils.builders

import commandmaster.CommandMaster
import commandmaster.components.CmdMastComponents
import commandmaster.components.UpgraderComponent
import commandmaster.macro.MacroCommand
import commandmaster.utils.commands.toCommandArg
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
import net.minecraft.component.Component
import net.minecraft.component.ComponentChanges
import net.minecraft.component.DataComponentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.*
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.RawFilteredPair
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.ColorHelper
import java.util.Optional

@JvmInline
value class ComponentsBuilder(val stack: Target){

    interface Target{
        fun<T> set(type: DataComponentType<T>, value: T)
    }

    class ItemStackTarget(val stack: ItemStack): Target{
        override fun<T> set(type: DataComponentType<T>, value: T){ stack.set(type, value) }
    }

    class ChangesTarget(val builder: ComponentChanges.Builder): Target{
        override fun<T> set(type: DataComponentType<T>, value: T){ builder.add(type, value) }
    }

    fun max_stack_size(size: Int) = stack.set(DataComponentTypes.MAX_STACK_SIZE, size)
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
    fun edible(hunger: Int=2, saturation: Float=1f) = add(DataComponentTypes.FOOD, FoodComponent.Builder().nutrition(hunger).saturationModifier(saturation).build())
    fun model(data: Int) = stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent(data))

    fun giving(target: TagKey<Item>?=null, merge: Boolean=false, show: Boolean=true, levelCost: Int=1, builder: ComponentsBuilder.()->Unit) {
        val subbuilder=ComponentChanges.builder()
        Items.COOKED_COD
        ComponentsBuilder(ChangesTarget(subbuilder)).builder()
        stack.set(CmdMastComponents.UPGRADER_COMPONENT, UpgraderComponent(Optional.ofNullable(target), subbuilder.build(),levelCost,show,merge))
    }

    fun entity(entity: NbtCompound) = stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(entity))

    fun entity(vararg pairs: Pair<String, NbtElement>) = entity(nbt(*pairs))

    inline fun book(title: String, author: String="Nobody", generation: Int=1, page_builder: BookBuilder.()->Unit){
        val pages= mutableListOf<RawFilteredPair<Text>>()
        BookBuilder(pages).page_builder()
        stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, WrittenBookContentComponent(RawFilteredPair.of(title),author,generation,pages,false))
    }

    companion object{
        fun stack(target: ItemStack) = ComponentsBuilder(ItemStackTarget(target))

        fun changes(target: ComponentChanges.Builder) = ComponentsBuilder(ChangesTarget(target))
    }
}

inline fun stack(item: Item, count: Int=1, builder: ComponentsBuilder.()->Unit): ItemStack{
    val stack=ItemStack(item,count)
    ComponentsBuilder.stack(stack).builder()
    return stack
}

fun String.withStacks(vararg stacks: Pair<Item,ComponentsBuilder.()->Unit>): String{
    val parts=this.split("%?%")
    val sb=StringBuilder()
    sb.append(parts[0])
    for(i in 1 until parts.size){
        if(i-1 in stacks.indices){
            val (item, builder)=stacks[i-1]
            val stack=item.defaultStack
            ComponentsBuilder.stack(stack).builder()
            sb.append(stack.toCommandArg())
        }
        sb.append(parts[i])
    }
    return sb.toString()
}