package commandmaster.group

import commandmaster.CommandMaster
import commandmaster.components.CmdMastComponents
import commandmaster.components.UpgraderComponent
import commandmaster.enchantments.CmdMastEnchantments
import commandmaster.enchantments.CmdMastEnchantments.MACRO_ATTACK
import commandmaster.item.CmdMastItems
import commandmaster.macro.MacroCommand
import commandmaster.utils.builders.ComponentsBuilder
import commandmaster.utils.builders.nbt
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
import net.minecraft.item.Items.*
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
            add(SADDLE){ name("saddle"); macro("ride @s mount \$s"); ench(MACRO_ATTACK to 1) }
            add(BOW){ name("implosion"); macro("summon tnt \$p"); ench(MACRO_ATTACK to 1) }
            add(GOLDEN_AXE){ name("lightning"); macro("summon lightning_bolt \$p"); ench(MACRO_ATTACK to 1) }

            // Tablet
            add(CmdMastItems.COMMAND_WAND){ name("xray"); color(255,255,0); model(19); macro("effect give @e[distance=1..30] minecraft:glowing 10") }
            add(CmdMastItems.COMMAND_WAND){ name("snow"); color(255,255,255); model(5); macro("execute positioned \$p run summon minecraft:snow_golem ~ ~1 ~") }
            add(CmdMastItems.COMMAND_WAND){ name("iron"); color(150,150,150); model(5); macro("execute positioned \$p run summon minecraft:iron_golem ~ ~1 ~") }

            // Enchanted Book
            add(ENCHANTED_BOOK){
                name("one_shot")
                lore("one_shot.desc")
                giving(ItemTags.WEAPON_ENCHANTABLE){
                    lore("one_shot.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("kill \$s")
                }
            }

            add(ENCHANTED_BOOK){
                name("pig")
                lore("pig.desc")
                giving(ItemTags.WEAPON_ENCHANTABLE){
                    lore("pig.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("execute at \$s summon pig run ride \$s mount @s")
                }
            }

            add(ENCHANTED_BOOK){
                name("slowness")
                lore("slowness.desc")
                giving(ItemTags.WEAPON_ENCHANTABLE){
                    lore("slowness.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("effect give \$s minecraft:slowness 10 1")
                }
            }

            add(ENCHANTED_BOOK){
                name("poisoned")
                lore("poisoned.desc")
                giving(ItemTags.WEAPON_ENCHANTABLE){
                    lore("poisoned.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("effect give \$s minecraft:poison 8")
                }
            }

            add(ENCHANTED_BOOK){
                name("edible")
                lore("edible.desc")
                giving{
                    lore("edible.desc")
                    edible()
                }
            }

            add(FLINT){
                name("flint")
                lore("flint.desc")
                giving{
                    ench(Enchantments.SHARPNESS to 2)
                }
            }

            // Eggs
            fun creeper(egg: Item, name: String, color: Int, macro: String) = add(egg) {
                name(CommandMaster.translatable("example","creeper_$name").append(" ").append(EGG.name))
                lore("creeper_$name.desc")
                entity(
                    "id" to "commandmaster:macro_creeper".nbt,
                    "color" to nbt(color),
                    "macro" to macro.nbt
                )
            }

            creeper(BLAZE_SPAWN_EGG, "lightning", -1188790,
                "summon lightning_bolt"
            )

            creeper(FROG_SPAWN_EGG, "fire", -1674473,
                "summon minecraft:fireball ~ ~ ~ {Motion:[0.0,-1.0,0.0],ExplosionPower:3}"
            )

            creeper(ZOMBIE_HORSE_SPAWN_EGG, "poison", -11112428,
                "multi (effect give @e[distance=..3] minecraft:poison 20 0 ; particle minecraft:dust 0 0.5 0 3 ~ ~ ~ 1.5 1.5 1.5 1 100)"
            )

            creeper(GHAST_SPAWN_EGG, "web", -2697514,
                "fill ~-1 ~-1 ~-1 ~1 ~1 ~1 minecraft:cobweb replace #replaceable"
            )

            creeper(ZOMBIE_SPAWN_EGG, "zombie", -11907030,
                "multi (summon zombie;summon skeleton;summon zombie;summon zombie)"
            )

            creeper(
                ENDERMAN_SPAWN_EGG, "dragon", -4914979,
                "summon dragon_fireball ~ ~ ~ {Motion:[0.,-1.,0.]}"
            )

            creeper(
                CHICKEN_SPAWN_EGG, "firework", -1857861,
                "multi (particle minecraft:firework ~ ~1 ~ 0 0 0 0.2 300 ;particle minecraft:flash)"
            )

            // Book
            val book= WRITTEN_BOOK.defaultStack
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