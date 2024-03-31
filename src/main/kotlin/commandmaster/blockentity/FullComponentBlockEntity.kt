package commandmaster.blockentity

import commandmaster.components.CmdMastComponents
import commandmaster.macro.MacroCommand
import net.minecraft.block.BlockState
import net.minecraft.block.DispenserBlock
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.component.Component
import net.minecraft.component.ComponentHolder
import net.minecraft.component.ComponentMap
import net.minecraft.component.ComponentMapImpl
import net.minecraft.component.DataComponentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.function.BiFunction
import java.util.function.UnaryOperator

class FullComponentBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState, base: ComponentMap=ComponentMap.EMPTY, val loaded: List<DataComponentType<*>>): ComponentBlockEntity(type, pos, state), ComponentHolder {

    private val components = ComponentMapImpl(base)

    override fun getComponents() = components

    fun <T> set(type: DataComponentType<T>, value: T) = components.set(type, value)

    fun <T, U> apply(type: DataComponentType<T>, defaultValue: T, change: U, applier: BiFunction<T, U, T>): T? {
        return this.set<T>(type, applier.apply(this.getOrDefault<T>(type, defaultValue), change))
    }

    fun <T> apply(type: DataComponentType<T>, defaultValue: T, applier: UnaryOperator<T>): T? {
        val `object` = this.getOrDefault(type, defaultValue)
        return this.set(type, applier.apply(`object`))
    }

    fun <T> remove(type: DataComponentType<out T>): T? {
        return components.remove(type)
    }

    override fun addComponents(builder: ComponentMap.Builder) {
        super.addComponents(builder)
        fun <T>add(comp: Component<T>) = builder.add(comp.type, comp.value)
        for(component in components) add(component)
        println(builder.build())
    }

    override fun readComponents(components: ComponentMap) {
        super.readComponents(components)
        fun <T>add(comp: DataComponentType<T>) = components.get(comp)?.let { set(comp, it) }
        for(type in loaded)add(type)
    }


}

fun BlockView.getComponentBlockEntity(pos: BlockPos): FullComponentBlockEntity? {
    return this.getBlockEntity(pos) as? FullComponentBlockEntity
}