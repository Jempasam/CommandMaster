package commandmaster.blockentity

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.component.ComponentHolder
import net.minecraft.component.DataComponentType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldAccess

class SimpleBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState): BlockEntity(type, pos, state), ComponentHolder {
}

fun<T> BlockEntity.get(type: DataComponentType<T>) = this.components.get(type)