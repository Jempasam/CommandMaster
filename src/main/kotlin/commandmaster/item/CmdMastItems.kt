package commandmaster.item

import commandmaster.CommandMaster
import commandmaster.block.CmdMastBlocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object CmdMastItems {

    val COMMAND_WAND= register("command_wand", CommandWandItem(Settings().maxCount(1)))

    val MACHINE_BLOCK= register("machine_block", CommandBlockItem(CmdMastBlocks.MACHINE_BLOCK, Settings().maxCount(16)))

    fun <T: Item> register(id: String, item: T): T{
        Registry.register(Registries.ITEM,CommandMaster/id,item)
        return item
    }
}