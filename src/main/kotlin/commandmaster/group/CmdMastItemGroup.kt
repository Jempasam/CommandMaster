package commandmaster.group

import commandmaster.CommandMaster
import commandmaster.components.CmdMastComponents
import commandmaster.item.CmdMastItems
import commandmaster.macro.MacroCommand
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.component.type.WrittenBookContentComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.RawFilteredPair
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.ColorHelper.Argb

object CmdMastItemGroup {
    val COMMAND_MASTER= register("command_master", FabricItemGroup.builder()
        .icon{CmdMastItems.COMMAND_WAND.defaultStack}
        .entries { context, entries ->

            fun add(item: Item, name: String?, red: Int, green: Int, blue: Int, macro: String?) {
                return entries.add(item.defaultStack.apply {
                    if(macro!=null)set(CmdMastComponents.MACRO_HOLDER, MacroCommand(macro))
                    if(name!=null)set(DataComponentTypes.CUSTOM_NAME, CommandMaster.translatable("example", name).styled{it.withItalic(false)})
                    if(red>=0)set(DataComponentTypes.DYED_COLOR, DyedColorComponent(Argb.getArgb(red,green,blue),false))
                })
            }

            // Command Wand
            add(CmdMastItems.COMMAND_WAND, "use_command", -1,-1,-1, null)
            add(CmdMastItems.COMMAND_WAND, "breaker", 120, 100, 100, "setblock \$p air destroy")
            add(CmdMastItems.COMMAND_WAND, "stonifier", 100, 100, 100, "setblock \$p minecraft:stone replace")
            add(CmdMastItems.COMMAND_WAND, "creeper", 0, 255, 0, "summon minecraft:creeper \$p")
            add(CmdMastItems.COMMAND_WAND, "tnt", 255, 0, 0, "summon minecraft:tnt \$p")

            // Machine Block
            add(CmdMastItems.MACHINE_BLOCK, "use_command", -1,-1,-1, null)
            add(CmdMastItems.MACHINE_BLOCK, "breaker", 255, 0, 0, "setblock ^ ^ ^1 air destroy")
            add(CmdMastItems.MACHINE_BLOCK, "fire", 255, 100, 0, "setblock ^ ^ ^1 fire keep")
            add(CmdMastItems.MACHINE_BLOCK, "aspirator", 200,210,255, "execute if block ^ ^ ^1 #minecraft:replaceable run multi (clone ^ ^ ^2 ^ ^ ^2 ^ ^ ^1 replace move;playsound minecraft:block.piston.contract ambient @a ~ ~ ~)")

            // Book
            val book= Items.WRITTEN_BOOK.defaultStack
            book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, WrittenBookContentComponent(
                RawFilteredPair.of("Small Guide of Command Master"),
                "Jempasam",
                1,
                listOf(
                    RawFilteredPair.of(Text.of("""
                        Commands:
                        - /command
                        - /get_color
                        - /multi
                    """.trimIndent()))
                ),
                false
            ))
            entries.add(book)
        })

        fun register(id: String, itemGroup: ItemGroup.Builder): ItemGroup{
            val ret=itemGroup
                .displayName(CommandMaster.translatable("item_group",id))
                .build()
            Registry.register(Registries.ITEM_GROUP,CommandMaster/id,ret)
            return ret
        }
}