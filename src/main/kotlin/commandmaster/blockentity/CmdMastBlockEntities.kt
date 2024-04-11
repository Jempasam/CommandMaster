package commandmaster.blockentity

import commandmaster.CommandMaster
import commandmaster.block.CmdMastBlocks
import commandmaster.components.CmdMastComponents
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.component.ComponentMap
import net.minecraft.component.DataComponentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.datafixer.TypeReferences
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos

object CmdMastBlockEntities {

    val MACHINE_BLOCK = component(
        "machine_block",
        CmdMastBlocks.MACHINE_BLOCK
    )

    fun component(id: String, vararg  blocks: Block)
        =register(id, *blocks){type, pos, state ->
            SimpleBlockEntity(type, pos, state)
        }

    fun <T : BlockEntity> register(id: String, vararg blocks: Block, factory: (BlockEntityType<T>,BlockPos,BlockState)->T) : BlockEntityType<T> {
        val holder: Array<BlockEntityType<T>?> = arrayOf(null)
        holder[0] = BlockEntityType.Builder.create({pos, state -> factory(holder[0]!!,pos,state)}, *blocks).build(Util.getChoiceType(TypeReferences.BLOCK_ENTITY, (CommandMaster/id).toString()))
        Registry.register(Registries.BLOCK_ENTITY_TYPE, CommandMaster/id, holder[0])
        return holder[0]!!
    }
}