package commandmaster.block

import commandmaster.CommandMaster
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.component.type.ContainerComponent
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object CmdMastBlocks {
    val MACHINE_BLOCK = register("machine_block", MachineBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))

    fun <T : Block> register(id: String, block: T): T {
        Registry.register(Registries.BLOCK, CommandMaster/id, block)
        return block
    }
}