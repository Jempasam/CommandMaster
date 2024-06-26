package commandmaster.group

import commandmaster.CommandMaster
import commandmaster.enchantments.CmdMastEnchantments.MACRO_ATTACK
import commandmaster.item.CmdMastItems.COMMAND_WAND
import commandmaster.item.CmdMastItems.MACHINE_BLOCK
import commandmaster.utils.builders.ComponentsBuilder
import commandmaster.utils.builders.nbt
import commandmaster.utils.builders.withStacks
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.component.Component
import net.minecraft.component.DataComponentType
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.Items.*
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.ItemTags.WEAPON_ENCHANTABLE
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Formatting.*

object CmdMastItemGroup {

    private infix fun<T> DataComponentType<T>.to(value: T) = Component(this, value)

    val COMMAND_MASTER= register("command_master", FabricItemGroup.builder()
        .icon{ COMMAND_WAND.defaultStack}
        .entries { context, entries ->

            fun add(item: Item, builder: ComponentsBuilder.()->Unit){
                val stack=item.defaultStack
                ComponentsBuilder(ComponentsBuilder.ItemStackTarget(stack)).builder()
                entries.add(stack)
            }

            // Command Wand
            add(COMMAND_WAND){ name("use_command") }
            add(COMMAND_WAND){ name("breaker"); color(120,100,100); macro("setblock \$p air destroy") }
            add(COMMAND_WAND){ name("stonifier"); color(100,100,100); macro("setblock \$p minecraft:stone replace") }
            add(COMMAND_WAND){ name("creeper"); color(0,255,0); macro("summon minecraft:creeper \$p") }
            add(COMMAND_WAND){ name("tnt"); color(255,0,0); macro("summon minecraft:tnt \$p") }
            add(COMMAND_WAND){ name("filler"); color(255,100,0); macro("fill \$1p \$2p \$0b") }
            add(COMMAND_WAND){ name("igniter"); color(100,200,70); macro("eexecute as @e[type=commandmaster:macro_creeper,distance=..20] run data modify entity @s ignited set value 1b")}
            add(COMMAND_WAND){
                name("terraformer")
                lore("terraformer.desc.1","terraformer.desc.2","terraformer.desc.3")
                color(0,100,0)
                model(21)
                macro("execute positioned ~ ~1.5 ~ run shootitem %?% 3".withStacks(
                    COMMAND_WAND to {
                        macro("fill ~-\$0-n ~-\$0-n ~-\$0-n ~\$0-n ~\$0-n ~\$0-n \$0b")
                        model(11)
                        color(0,100,0)
                    }
                ))
            }
            add(COMMAND_WAND){
                name("terraformer2")
                lore("terraformer2.desc")
                color(80,100,0)
                model(21)
                macro("execute positioned ~ ~1.5 ~ run shootitem %?% 3".withStacks(
                    COMMAND_WAND to {
                        macro("execute each ~-10 ~-10 ~-10 ~10 ~10 ~10 unless block ~ ~ ~ air if block ~ ~-1 ~ #replaceable run clone ~ ~ ~ ~ ~ ~ ~ ~-1 ~ replace move")
                        model(11)
                        color(80,100,0)
                    }
                ))
            }
            add(COMMAND_WAND){
                name("terraformer3")
                lore("terraformer3.desc.1","terraformer.desc.2","terraformer.desc.3")
                color(0,100,80)
                model(21)
                macro("execute positioned ~ ~1.5 ~ run shootitem %?% 3".withStacks(
                    COMMAND_WAND to {
                        macro("execute each ~-\$0-n ~-\$0-n ~-\$0-n ~\$0-n ~\$0-n ~\$0-n unless block ~ ~ ~ air run setblock ~ ~ ~ \$0b")
                        model(11)
                        color(0,100,80)
                    }
                ))
            }
            add(COMMAND_WAND){
                name("terraformer4")
                lore("terraformer4.desc.1","terraformer.desc.2","terraformer.desc.3")
                color(0,40,0)
                model(21)
                macro("execute positioned ~ ~1.5 ~ run shootitem %?% 3".withStacks(
                    COMMAND_WAND to {
                        macro("fill ~-\$0-n ~-\$0-n ~-\$0-n ~\$0-n ~\$0-n ~\$0-n air replace \$0b")
                        model(11)
                        color(0,40,0)
                    }
                ))
            }

            // Machine Block
            add(MACHINE_BLOCK){ name("use_command") }
            add(MACHINE_BLOCK){ name("breaker"); color(255,0,0); macro("setblock ^ ^ ^1 air destroy") }
            add(MACHINE_BLOCK){ name("fire"); color(255,100,0); macro("setblock ^ ^ ^1 fire keep") }
            add(MACHINE_BLOCK){ name("aspirator"); color(200,210,255); macro("execute if block ^ ^ ^1 #minecraft:replaceable run multi (clone ^ ^ ^2 ^ ^ ^2 ^ ^ ^1 replace move;playsound minecraft:block.piston.contract ambient @a ~ ~ ~)") }

            // Attack
            add(SADDLE){ name("saddle"); macro("ride @s mount \$s"); ench(MACRO_ATTACK to 1) }
            add(BOW){ name("implosion"); macro("summon tnt \$p"); ench(MACRO_ATTACK to 1) }
            add(GOLDEN_AXE){ name("lightning"); macro("summon lightning_bolt \$p"); ench(MACRO_ATTACK to 1) }
            add(COMMAND_WAND){
                name("bomb"); color(40,20,20); model(11)
                max_stack_size(16)
                macro("execute positioned ~ ~1.5 ~ run shootitem %?%".withStacks(
                    COMMAND_WAND to {
                        name("bomb")
                        color(40,20,20)
                        model(11)
                        macro("summon tnt ~ ~ ~ {Fuse:80}")
                    }
                ))
            }

            // Tablet
            add(COMMAND_WAND){ name("xray"); color(255,255,0); model(19); macro("effect give @e[distance=1..30] minecraft:glowing 10") }
            add(COMMAND_WAND){ name("shovel"); color(-7781357); model(19); macro("execute positioned \$p each ~-1 ~-1 ~-1 ~1 ~1 ~1 if block ~ ~ ~ #mineable/shovel run setblock ~ ~ ~ air destroy")}
            add(COMMAND_WAND){ name("snow"); color(255,255,255); model(5); macro("execute positioned \$p run summon minecraft:snow_golem ~ ~1 ~") }
            add(COMMAND_WAND){ name("iron"); color(150,150,150); model(5); macro("execute positioned \$p run summon minecraft:iron_golem ~ ~1 ~") }

            // Enchanted Book
            add(ENCHANTED_BOOK){
                name("one_shot")
                lore("one_shot.desc")
                giving(WEAPON_ENCHANTABLE){
                    lore("one_shot.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("kill \$s")
                }
            }

            add(ENCHANTED_BOOK){
                name("pig")
                lore("pig.desc")
                giving(WEAPON_ENCHANTABLE){
                    lore("pig.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("execute at \$s summon pig run ride \$s mount @s")
                }
            }

            add(ENCHANTED_BOOK){
                name("slowness")
                lore("slowness.desc")
                giving(WEAPON_ENCHANTABLE){
                    lore("slowness.desc")
                    ench(MACRO_ATTACK to 1)
                    macro("effect give \$s minecraft:slowness 10 1")
                }
            }

            add(ENCHANTED_BOOK){
                name("poisoned")
                lore("poisoned.desc")
                giving(WEAPON_ENCHANTABLE){
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
                giving(merge=true){
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
                "execute each ~-\$n ~ ~-\$n ~\$n ~ ~\$n run summon lightning_bolt"
            )

            creeper(FROG_SPAWN_EGG, "fire", -1674473,
                "execute repeat \$+n run summon minecraft:fireball ~ ~ ~ {Motion:[0.0,-1.0,0.0],ExplosionPower:3}"
            )

            creeper(ZOMBIE_HORSE_SPAWN_EGG, "poison", -11112428,
                "multi (effect give @e[distance=..3] minecraft:poison 20 \$n ; particle minecraft:dust{color:[0.0,0.5,0.0],scale:3} ~ ~ ~ 1.5 1.5 1.5 1 100)"
            )

            creeper(ENDERMITE_SPAWN_EGG, "blindness", -12763847,
                "multi (effect give @e[distance=..3] minecraft:blindness \$n20 0 ; particle campfire_cosy_smoke ~ ~1 ~ 0 0.5 0 0.2 100)"
            )

            creeper(GHAST_SPAWN_EGG, "web", -2697514,
                "multi (fill ~-\$+n ~-\$+n ~-\$+n ~\$+n ~\$+n ~\$+n minecraft:cobweb replace #replaceable)"
            )

            creeper(ZOMBIE_SPAWN_EGG, "zombie", -11907030,
                "execute repeat \$+n run multi (execute repeat 3 run summon zombie;execute repeat 1 run summon skeleton)"
            )

            creeper(VILLAGER_SPAWN_EGG, "breaker", -7707835,
                "fill ~-2 ~-2 ~-2 ~2 ~2 ~2 air destroy"
            )

            creeper(
                ENDERMAN_SPAWN_EGG, "dragon", -4914979,
                "execute repeat \$+n run summon dragon_fireball ~ ~ ~ {Motion:[0.,-1.,0.]}"
            )

            creeper(
                CHICKEN_SPAWN_EGG, "firework", -1857861,
                "execute repeat \$+n run multi (particle minecraft:firework ~ ~1 ~ 0 0 0 0.2 300 ;particle minecraft:flash)"
            )

            creeper(
                SPIDER_SPAWN_EGG, "super", -11532537,
                "execute repeat \$+n repeat 5 run summon minecraft:bat ~ ~ ~ {Passengers:[{id:\"tnt\",fuse:200}]}"
            )

            // Team
            fun team(name: String, color: Formatting){
                add(COMMAND_WAND){
                    name("team_$name")
                    model(10)
                    color(color.colorValue ?: DyeColor.WHITE.fireworkColor)
                    lore("team_$name.desc")
                    macro("multi (team add $name \"${name.replaceFirstChar{it.uppercase()}}\";team modify $name color ${color.name};team join $name \$s)")
                }
            }

            team("red", RED)
            team("blue", BLUE)
            team("green", GREEN)
            team("yellow", YELLOW)

            add(COMMAND_WAND){
                name("team_none")
                model(10)
                color(DyeColor.WHITE.fireworkColor)
                lore("team_none.desc")
                macro("team leave \$s")
            }

            // Book
            val book= WRITTEN_BOOK.defaultStack
            add(WRITTEN_BOOK){
                book("Small Guide of Command Master", "Jempasam"){
                    fun page(title: String, content: String, vararg examples: String){
                        val result=Text.empty()
                        result.append(Text.literal("$title\n").formatted(BOLD))
                        result.append(Text.literal("$content\n").formatted(DARK_GRAY))
                        for(example in examples){
                            result.append(Text.literal("$example\n").formatted(ITALIC, GRAY).styled {
                                it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND,example))
                            })
                        }
                        +result
                    }
                    page(
                        "--Guide Book--",
                        """
                            Learn the many commands.
                            Use "/cmdmast example" while holding any example item to learn how it works.
                        """.trimIndent()
                    )
                    page("/cmdmast", "Show the components of an items with a colored output, and get help about the mod", "/cmdmast example")
                    page("/get_color", "Get the color under the mouse pointer. Client-side only.", "/get_color")
                    page("/multi", "Execute multiple commands at once", "/multi (say Hello;say World)", "/multi (effect give @s speed;effect give @s jump_boost)")
                    page("/execute repeat", "Repeat a command multiple times", "/execute repeat 5 run say Hello","/execute repeat 5 run summon pig")
                    page("/macro", "Debug macros, create items holding a macro, view the macro manual.", "/macro wand setblock \$p air destroy")
                    page("/fetch_nbt", "Fetch a nbt value from a external source through HTTP, NBT format is sort of a subset of JSON", """/fetchnbt "https://api.ipify.org?format=json" commandmaster:example ip tellraw @s {"storage":"commandmaster:example","nbt":"ip"}""")
                    page("/fix", "Fix a command block using a regex and replacement string.","""/fix ~ ~-1 ~ "say " "tellraw @s """")
                    page("/execute each", "Call a command for each block in a rectangular zone.", """/execute each ~-2 ~-2 ~-2 ~2 ~2 ~2 run particle minecraft:block_marker fire""", """/execute each ~-1 ~-1 ~-1 ~1 ~1 ~1 run summon minecraft:shulker""")
                }
            }
        })

        fun register(id: String, itemGroup: ItemGroup.Builder): ItemGroup{
            val ret=itemGroup
                .displayName(CommandMaster.translatable("item_group",id))
                .build()
            Registry.register(Registries.ITEM_GROUP,CommandMaster/id,ret)
            return ret
        }
}