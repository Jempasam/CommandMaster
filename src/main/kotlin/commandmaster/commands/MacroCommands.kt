package commandmaster.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import commandmaster.CommandMaster
import commandmaster.commands.arguments.MacroCommandArgumentType
import commandmaster.components.CmdMastComponents
import commandmaster.enchantments.CmdMastEnchantments
import commandmaster.item.CmdMastItems
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroCompletion
import commandmaster.macro.MacroParamType
import commandmaster.utils.commands.help
import commandmaster.utils.commands.toCommandArg
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.command.argument.NbtElementArgumentType
import net.minecraft.item.ItemStack
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.server.command.ServerCommandSource as SCS

object MacroCommands: CommandRegistrationCallback {
    override fun register(disp: CommandDispatcher<SCS>, reg: CommandRegistryAccess, env: CommandManager.RegistrationEnvironment) {

        fun give_macro(context: CommandContext<SCS>, stack_provider: ()->ItemStack): Int{
            val player=context.source.player
            if(player!=null){
                val stack=stack_provider()
                stack.set(CmdMastComponents.MACRO_HOLDER, context.getArgument("macro",MacroCommand::class.java))
                player.giveItemStack(stack)
                return 1
            }
            else return 0
        }

        /**
         * A macro wand that run a macro command when right clicked.
         */
        val WAND= literal<SCS>("wand").then(
            argument<SCS, _>("macro", MacroCommandArgumentType).executes{
                give_macro(it){CmdMastItems.COMMAND_WAND.defaultStack}
            }
        ).help {
            Text.literal("""
                    Give a wand that run a macro command when right clicked.
                    Each click on a block fill the next macro argument with the block.
                    Each click on an entity fill the next macro argument with the entity.
                    Each click on the air fill the next macro argument with the player as entity.
                    You can right click with the item on an empty slot to create a new wand with the currents macro arguments inlineds;
                    You can right click on the wand with an item in your inventory to fill the next macro argument with the item.
                    You can right click on the wand with no item in your inventory to clear the current macro arguments.
                    """.trimIndent())
        }

        /**
         * An item holding a macro
         */
        val ITEM= literal<SCS>("item").then(
            argument<SCS, _>("item", ItemStackArgumentType.itemStack(reg)).then(
                argument<SCS, _>("macro", MacroCommandArgumentType).executes{
                    give_macro(it){ItemStackArgumentType.getItemStackArgument(it,"item").createStack(1,true)}
                }
            )
        )

        /**
         * An item that call a macro when the wearer is attacked with the attacking entity as parameters.
         */
        val THORNS= literal<SCS>("thorns").then(
            argument<SCS, _>("item", ItemStackArgumentType.itemStack(reg)).then(
                argument<SCS, _>("macro", MacroCommandArgumentType).executes{
                    give_macro(it){
                        val stack= ItemStackArgumentType.getItemStackArgument(it,"item").createStack(1,true)
                        stack.addEnchantment(CmdMastEnchantments.MACRO_THORNS,1)
                        stack
                    }
                }
            )
        ).help {
            Text.of("Give an item that call a macro when the wearer is attacked with the attacking entity as parameters.")
        }

        /**
         * An item that call a macro when the wearer attack with the attacked entity as parameters.
         */
        val ATTACK= literal<SCS>("attack").then(
            argument<SCS, _>("item", ItemStackArgumentType.itemStack(reg)).then(
                argument<SCS, _>("macro", MacroCommandArgumentType).executes{
                    give_macro(it){
                        val stack= ItemStackArgumentType.getItemStackArgument(it,"item").createStack(1,true)
                        stack.addEnchantment(CmdMastEnchantments.MACRO_ATTACK,1)
                        stack
                    }
                }
            )
        ).help {
            Text.of("Give an item that call a macro a attack with attacked entity as parameters.")
        }

        /**
         * A macro machine.
         */
        val MACHINE= literal<SCS>("machine").then(
            argument<SCS, _>("macro", MacroCommandArgumentType).executes{
                give_macro(it){CmdMastItems.MACHINE_BLOCK.defaultStack}
            }
        )

        /**
         * Show the macro.
         */
        val SHOW= literal<SCS>("show").then(
            argument<SCS, _>("macro", MacroCommandArgumentType).executes{
                val macro= it.getArgument("macro",MacroCommand::class.java)
                it.source.sendMessage(macro.text)
                macro.parameters.size
            }
        )

        /*
         * Thrown wand
         */
        val THROWN= literal<SCS>("thrown").then(
            argument<SCS, _>("strength",DoubleArgumentType.doubleArg(0.0)).then(
                argument<SCS,_>("macro", MacroCommandArgumentType).executes{
                    val strength= DoubleArgumentType.getDouble(it,"strength")
                    val macro= it.getArgument("macro",MacroCommand::class.java)

                    val thrown=CmdMastItems.COMMAND_WAND.defaultStack
                    thrown.set(CmdMastComponents.MACRO_HOLDER, macro)

                    val thrower=CmdMastItems.COMMAND_WAND.defaultStack
                    thrower.set(CmdMastComponents.MACRO_HOLDER, MacroCommand("execute anchored eyes positioned ^ ^ ^ run shootitem ${thrown.toCommandArg()} $strength"))

                    it.source.player?.giveItemStack(thrower)
                    1
                }
            )
        )

            /**argument<SCS, _>("macro", MacroCommandArgumentType).executes{
            give_macro(it){CmdMastItems.COMMAND_WAND.defaultStack}
            }
         * Fill the last parameter
         */
        fun<T> give_do(context: CommandContext<SCS>, getter: ()->T?, of: MacroParamType.(T)->String?): Int{
            fun error(msg:String): Int{
                context.source.sendError(CommandMaster.translatable("message",msg))
                return 0
            }

            val player=context.source.player ?: return error("need_player")

            var stack=player.mainHandStack
            if(stack.isEmpty || stack.item!=CmdMastItems.COMMAND_WAND)stack=player.offHandStack
            if(stack.isEmpty || stack.item!=CmdMastItems.COMMAND_WAND) return error("need_wand")

            val macro=stack.get(CmdMastComponents.MACRO_HOLDER) ?: return error("need_macro_wand")

            val compo=stack.get(CmdMastComponents.MACRO_COMPLETION) ?: MacroCompletion()
            if(compo.size<macro.parameters.size){
                val value=getter() ?: return error("invalid_target")
                try{
                    stack.set(CmdMastComponents.MACRO_COMPLETION,compo.builder().add(value,of,macro).build())
                }catch (e:MacroCompletion.InvalidTarget){ return error("invalid_target") }
            }
            return 1
        }

        val SELECT=literal<SCS>("select").then(
            literal<SCS>("item").then(
                argument<SCS,_>("item",ItemStackArgumentType.itemStack(reg)).executes{
                    give_do( it,
                        {ItemStackArgumentType.getItemStackArgument(it,"item").createStack(1,false)},
                        MacroParamType::of
                    )
                }
            )
        ).then(
            literal<SCS>("entity").then(
                argument<SCS,_>("entity", EntityArgumentType.entity()).executes{
                    give_do( it,
                        { EntityArgumentType.getEntities(it,"entity").firstOrNull()},
                        MacroParamType::of
                    )
                }
            )
        ).then(
            literal<SCS>("block").then(
                argument<SCS,_>("block", BlockPosArgumentType.blockPos()).executes{ context->
                    give_do( context,
                        {
                            BlockPosArgumentType.getBlockPos(context,"block").toImmutable()
                                ?.let { CachedBlockPosition(context.source.world,it,true) }
                        },
                        MacroParamType::of
                    )
                }
            )
        ).then(
            literal<SCS>("string").then(
                argument<SCS,_>("string", StringArgumentType.greedyString()).executes{ context->
                    give_do( context,
                        { StringArgumentType.getString(context,"string")},
                        MacroParamType::of
                    )
                }
            )
        ).then(
            literal<SCS>("data").then(
                argument<SCS,_>("data", NbtElementArgumentType.nbtElement()).executes{ context->
                    give_do( context,
                        { NbtElementArgumentType.getNbtElement(context,"data")},
                        MacroParamType::of
                    )
                }
            )
        ).help{
            Text.of("Fill the last parameter of the wand with the given target.")
        }

        val PARAMETER= literal<SCS>("parameter")
        for((key,param) in MacroParamType.TYPES){
            PARAMETER.then(
                literal<SCS>(param.name).executes{ context ->
                    context.source.sendMessage(Text.literal("""
                    ${param.name}($key):
                      Example: ${param.example}
                      On player: ${context.source.player?.let{param.of(it)}}
                      On the block under the player: ${context.source.player?.let{param.of(CachedBlockPosition(context.source.world,it.blockPos.down(),true))}}
                      On the player name: ${context.source.player?.let{param.of(it.name.string)}}
                      On the item the player hold: ${context.source.player?.let{param.of(it.mainHandStack)}}
                    """.trimIndent()).withColor(param.color))
                    1
                }
            )
        }

        val MACRO= literal<SCS>("macro")
            .then(WAND)
            .then(ITEM)
            .then(THORNS)
            .then(ATTACK)
            .then(MACHINE)
            .then(THROWN)
            .then(SHOW)
            .then(SELECT)
            .then(PARAMETER)
            .help {
                val message=Text.literal("""
                            Create items that run macro commands.
                            Macro commands are normal commands with parameters.
                            The parameters can be one of the following:
                            """.trimIndent())
                for((key,param) in MacroParamType.TYPES){
                    val value=it.source.player?.let { ", Example: "+param.of(it) } ?: ""
                    message.append("\n - ").append(Text.literal("$$key, ${param.name}$value").withColor(param.color))
                }
                message
            }


        disp.register(MACRO)
    }
}