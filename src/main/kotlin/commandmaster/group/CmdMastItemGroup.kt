package commandmaster.group

import commandmaster.CommandMaster
import commandmaster.components.CmdMastComponents
import commandmaster.components.UpgraderComponent
import commandmaster.enchantments.CmdMastEnchantments
import commandmaster.enchantments.CmdMastEnchantments.MACRO_ATTACK
import commandmaster.item.CmdMastItems
import commandmaster.macro.MacroCommand
import commandmaster.utils.builders.ComponentsBuilder
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.component.Component
import net.minecraft.component.ComponentChanges
import net.minecraft.component.ComponentHolder
import net.minecraft.component.DataComponentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.CustomModelDataComponent
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.WrittenBookContentComponent
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.RawFilteredPair
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.ColorHelper.Argb

object CmdMastItemGroup {

    private infix fun<T> DataComponentType<T>.to(value: T) = Component(this, value)

    val COMMAND_MASTER= register("command_master", FabricItemGroup.builder()
        .icon{CmdMastItems.COMMAND_WAND.defaultStack}
        .entries { context, entries ->

            fun add(item: Item, builder: ComponentsBuilder.()->Unit){
                val stack=item.defaultStack
                ComponentsBuilder(ComponentsBuilder.ItemStackTarget(stack)).builder()
                entries.add(stack)
            }

            // Command Wand
            add(CmdMastItems.COMMAND_WAND){ name("use_command") }
            add(CmdMastItems.COMMAND_WAND){ name("breaker"); color(120,100,100); macro("setblock \$p air destroy") }
            add(CmdMastItems.COMMAND_WAND){ name("stonifier"); color(100,100,100); macro("setblock \$p minecraft:stone replace") }
            add(CmdMastItems.COMMAND_WAND){ name("creeper"); color(0,255,0); macro("summon minecraft:creeper \$p") }
            add(CmdMastItems.COMMAND_WAND){ name("tnt"); color(255,0,0); macro("summon minecraft:tnt \$p") }
            add(CmdMastItems.COMMAND_WAND){ name("filler"); color(255,100,0); macro("fill \$p \$p \$b") }

            // Machine Block
            add(CmdMastItems.MACHINE_BLOCK){ name("use_command") }
            add(CmdMastItems.MACHINE_BLOCK){ name("breaker"); color(255,0,0); macro("setblock ^ ^ ^1 air destroy") }
            add(CmdMastItems.MACHINE_BLOCK){ name("fire"); color(255,100,0); macro("setblock ^ ^ ^1 fire keep") }
            add(CmdMastItems.MACHINE_BLOCK){ name("aspirator"); color(200,210,255); macro("execute if block ^ ^ ^1 #minecraft:replaceable run multi (clone ^ ^ ^2 ^ ^ ^2 ^ ^ ^1 replace move;playsound minecraft:block.piston.contract ambient @a ~ ~ ~)") }

            // Attack
            add(Items.SADDLE){ name("saddle"); macro("ride @s mount \$s"); ench(MACRO_ATTACK to 1) }
            add(Items.BOW){ name("implosion"); macro("summon tnt \$p"); ench(MACRO_ATTACK to 1) }
            add(Items.GOLDEN_AXE){ name("lightning"); macro("summon lightning_bolt \$p"); ench(MACRO_ATTACK to 1) }

            // Tablet
            add(CmdMastItems.COMMAND_WAND){ name("xray"); color(255,255,0); model(19); macro("effect give @e[distance=1..30] minecraft:glowing 10") }
            add(CmdMastItems.COMMAND_WAND){ name("snow"); color(255,255,255); model(5); macro("execute positioned \$p run summon minecraft:snow_golem ~ ~1 ~") }
            add(CmdMastItems.COMMAND_WAND){ name("iron"); color(150,150,150); model(5); macro("execute positioned \$p run summon minecraft:iron_golem ~ ~1 ~") }

            // Enchanted Book
            add(Items.ENCHANTED_BOOK){
                name("one_shot")
                lore("one_shot.desc")
                giving(ItemTags.WEAPON_ENCHANTABLE){
                    lore("one_shot.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("kill \$s")
                }
            }

            add(Items.ENCHANTED_BOOK){
                name("pig")
                lore("pig.desc")
                giving(ItemTags.WEAPON_ENCHANTABLE){
                    lore("pig.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("execute at \$s summon pig run ride \$s mount @s")
                }
            }

            add(Items.ENCHANTED_BOOK){
                name("slowness")
                lore("slowness.desc")
                giving(ItemTags.WEAPON_ENCHANTABLE){
                    lore("slowness.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("effect give \$s minecraft:slowness 10 1")
                }
            }

            add(Items.ENCHANTED_BOOK){
                name("poisoned")
                lore("poisoned.desc")
                giving(ItemTags.WEAPON_ENCHANTABLE){
                    lore("poisoned.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("effect give \$s minecraft:poison 8")
                }
            }

            add(Items.ENCHANTED_BOOK){
                name("edible")
                lore("edible.desc")
                giving{
                    lore("edible.desc")
                    edible()
                }
            }

            add(Items.FLINT){
                name("flint")
                lore("flint.desc")
                giving{
                    ench(Enchantments.SHARPNESS to 2)
                }
            }

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
                        Use "/command example" while holding any example item to learn how it works.
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