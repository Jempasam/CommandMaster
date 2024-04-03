package commandmaster.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.*
import com.mojang.brigadier.builder.RequiredArgumentBuilder.*
import com.mojang.brigadier.context.CommandContext
import commandmaster.blockentity.FullComponentBlockEntity
import commandmaster.components.CmdMastComponents
import commandmaster.enchantments.CmdMastEnchantments
import commandmaster.files.FileSystem
import commandmaster.item.CmdMastItems
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroParamType
import commandmaster.utils.nbt.toText
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.block.entity.CommandBlockBlockEntity
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.command.argument.RegistryEntryArgumentType
import net.minecraft.command.argument.RegistryKeyArgumentType
import net.minecraft.component.Component
import net.minecraft.component.DataComponentType
import net.minecraft.enchantment.Enchantment
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.screen.AnvilScreenHandler
import net.minecraft.screen.EnchantmentScreenHandler
import net.minecraft.server.command.ServerCommandSource as SCS
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.nio.charset.Charset
import kotlin.jvm.optionals.getOrNull


object CmdMastCommands {
    init{
        CommandRegistrationCallback.EVENT.register{ disp, reg, man ->

            // Action that give item
            val WAND=literal<SCS>("wand").then(
                argument<SCS,_>("macro",MacroCommandArgumentType).executes{
                    val player=it.source.player
                    if(player!=null){
                        val stack=CmdMastItems.COMMAND_WAND.defaultStack
                        stack.set(CmdMastComponents.MACRO_HOLDER,MacroCommand(it.getArgument("macro",String::class.java)))
                        player.giveItemStack(stack)
                        1
                    }
                    else 0
                }
            )
            val ITEM=literal<SCS>("item").then(
                argument<SCS,_>("item",ItemStackArgumentType.itemStack(reg)).then(
                    argument<SCS,_>("macro",MacroCommandArgumentType).executes{
                        val player=it.source.player
                        if(player!=null){
                            val stack=ItemStackArgumentType.getItemStackArgument(it,"item").createStack(1,true)
                            stack.set(CmdMastComponents.MACRO_HOLDER,MacroCommand(it.getArgument("macro",String::class.java)))
                            player.giveItemStack(stack)
                            1
                        }
                        else 0
                    }
                )
            )
            val THORNS=literal<SCS>("thorns").then(
                argument<SCS,_>("item",ItemStackArgumentType.itemStack(reg)).then(
                    argument<SCS,_>("macro",MacroCommandArgumentType).executes{
                        val player=it.source.player
                        if(player!=null){
                            val stack=ItemStackArgumentType.getItemStackArgument(it,"item").createStack(1,true)
                            stack.addEnchantment(CmdMastEnchantments.MACRO_THORNS,1)
                            stack.set(CmdMastComponents.MACRO_HOLDER,MacroCommand(it.getArgument("macro",String::class.java)))
                            player.giveItemStack(stack)
                            1
                        }
                        else 0
                    }
                )
            ).help {
                Text.of("Give an item that call a macro when the wearer is attacked with the attacking entity as parameters.")
            }
            val ATTACK=literal<SCS>("attack").then(
                argument<SCS,_>("item",ItemStackArgumentType.itemStack(reg)).then(
                    argument<SCS,_>("macro",MacroCommandArgumentType).executes{
                        val player=it.source.player
                        if(player!=null){
                            val stack=ItemStackArgumentType.getItemStackArgument(it,"item").createStack(1,true)
                            stack.addEnchantment(CmdMastEnchantments.MACRO_ATTACK,1)
                            stack.set(CmdMastComponents.MACRO_HOLDER,MacroCommand(it.getArgument("macro",String::class.java)))
                            player.giveItemStack(stack)
                            1
                        }
                        else 0
                    }
                )
            ).help {
                Text.of("Give an item that call a macro a attack with attacked entity as parameters.")
            }
            val MACHINE=literal<SCS>("machine").then(
                argument<SCS,_>("macro",MacroCommandArgumentType).executes{
                    val player=it.source.player
                    if(player!=null){
                        val stack=CmdMastItems.MACHINE_BLOCK.defaultStack
                        stack.set(CmdMastComponents.MACRO_HOLDER,MacroCommand(it.getArgument("macro",String::class.java)))
                        player.giveItemStack(stack)
                        1
                    }
                    else 0
                }
            )
            val SHOW=literal<SCS>("show").then(
                argument<SCS,_>("macro",MacroCommandArgumentType).executes{
                    val macro=MacroCommand(it.getArgument("macro",String::class.java))
                    it.source.sendMessage(macro.text)
                    macro.parameters.size
                }
            )

            val EXAMPLE=literal<SCS>("example").executes{
                val player=it.source.player
                if(player!=null){
                    val stack=player.mainHandStack
                    val sb=Text.literal("[${Registries.ITEM.getId(stack.item)}]{\n")
                    for(comp in stack.components){
                        fun<T> apply(comp: Component<T>){
                            val nbt=comp.type.codecOrThrow.encodeStart(NbtOps.INSTANCE,comp.value)
                            val key=Registries.DATA_COMPONENT_TYPE.getId(comp.type)
                            sb.append("  [$key]: ").append(nbt.result().getOrNull()?.toText(2)?:Text.literal("null")).append("\n")
                        }
                        apply(comp)
                    }
                    sb.append("}[ ]")
                    it.source.sendMessage(sb)
                    1
                }
                else{
                    it.source.sendError(Text.literal("Need Player!"))
                    0
                }
            }.help {
                Text.of("Show the components of the item in the main hand of the player. Use it to see how the example items are made.")
            }

            val COMMAND= literal<SCS>("command").requires {it.hasPermissionLevel(2)}
                .then(WAND)
                .then(SHOW)
                .then(MACHINE)
                .then(ITEM)
                .then(THORNS)
                .then(ATTACK)
                .then(EXAMPLE)
                .help {
                    val message=Text.literal("""
                            Create items that run macro commands.
                            Macro commands are normal commands with parameters.
                            The parameters can be one of the following:
                            """.trimIndent())
                    for((key,param) in MacroParamType.TYPES){
                        val value=it.source.player?.let { ", Example: "+param.selectAir(it) } ?: ""
                        message.append("\n - ").append(Text.literal("$$key, ${param.name}$value)").withColor(param.color))
                    }
                    message
                }

            val FILE= literal<SCS>("file").requires {it.hasPermissionLevel(2)}.then(
                literal<SCS>("exist").then(
                    argument<SCS,_>("path",StringArgumentType.string()).executes{
                        FileSystem(it.source.server).exist(FileSystem.getPath("path",it)).let { if(it) 1 else 0 }
                    }
                )
            ).then(
                literal<SCS>("list").then(
                    argument<SCS,_>("path",StringArgumentType.string()).executes{
                        val content=FileSystem(it.source.server).list(FileSystem.getPath("path",it))
                        it.source.sendMessage(Text.of("\nDIRECTORIES:\n"+content.joinToString("\n - ")))
                        1
                    }
                )
            ).then(
                literal<SCS>("tree").then(
                    argument<SCS,_>("path",StringArgumentType.string()).executes{
                        val sbuffer=StringBuilder()
                        sbuffer.append("\nFILES:\n")
                        FileSystem(it.source.server).tree(FileSystem.getPath("path",it)){depth,file,mpath->
                            sbuffer.append("  "+"  ".repeat(depth))
                            sbuffer.append(file)
                            sbuffer.append("\n")
                        }
                        it.source.sendMessage(Text.of(sbuffer.toString()))
                        1
                    }
                )
            ).then(
                literal<SCS>("mkdir").then(
                    argument<SCS,_>("path",StringArgumentType.string()).executes{
                        FileSystem(it.source.server).mkdir(FileSystem.getPath("path",it)).let { if(it) 1 else 0 }
                    }
                )
            ).then(
                literal<SCS>("write").then(
                    argument<SCS,_>("path",StringArgumentType.string()).then(
                        argument<SCS,_>("content",StringArgumentType.string()).executes{
                            val content=it.getArgument("content",String::class.java).replace("\$LR$","\n").byteInputStream()
                            val path=FileSystem(it.source.server).write(FileSystem.getPath("path",it),content)
                            1
                        }
                    )
                )
            ).then(
                literal<SCS>("read").then(
                    argument<SCS,_>("path",StringArgumentType.string()).executes exec@{ context->
                        val input=FileSystem(context.source.server).read(FileSystem.getPath("path",context))
                        input.onSuccess {
                            var content=it.readBytes().toString(Charset.defaultCharset())
                            var linecount=2
                            content="|1  | "+content.replace(Regex("\n")){"\n|${(linecount++).toString().padEnd(3,' ')}| "}
                            context.source.sendMessage(Text.of("\n|---| CONTENT:\n$content"))
                            return@exec 1
                        }.onFailure {
                            context.source.sendMessage(Text.literal("No such file!").withColor(Colors.RED))
                            return@exec 0
                        }
                        0
                    }
                )
            )

            val RUNSTACK=literal<SCS>("runstack").then(
                argument<SCS,_>("position",BlockPosArgumentType.blockPos()).executes{
                    var pos=BlockPosArgumentType.getBlockPos(it,"position")
                    while(!it.source.world.getBlockState(pos).isAir){
                        val blockentity=it.source.world.getBlockEntity(pos)
                        val command: String
                        if(blockentity is CommandBlockBlockEntity){
                            command=blockentity.commandExecutor.command
                            it.source.server.commandManager.executeWithPrefix(it.source,command)
                        }
                        else if(blockentity is FullComponentBlockEntity){
                            command= blockentity.get(CmdMastComponents.MACRO_HOLDER) ?.build(listOf()) ?: break
                            MacroCommand.executeMultiline(it.source.server,it.source,command)
                        }
                        else break
                        pos=pos.up()
                    }
                    1
                }
            ).help{
                Text.of("Run a stack of command blocks or machines from the bottom to top, and with the current source(location, entity, etc.).")
            }

            val MULTI=literal<SCS>("multi").then(
                argument<SCS,_>("commands",MultiCommandArgumentType).executes{
                    val commands=it.getArgument("commands",List::class.java) as List<String>
                    for(command in commands){
                        println(command)
                        it.source.server.commandManager.executeWithPrefix(it.source,command)
                    }
                    1
                }
            ).help{
                Text.of("Run multiple commands separated by semicolons.")
            }

            disp.register(COMMAND)
            //disp.register(FILE)
            disp.register(RUNSTACK)
            disp.register(MULTI)
        }
    }

    private fun<T : ArgumentBuilder<SCS, T>> T.help(msg:(CommandContext<SCS>)->Text): T{
        this.then(literal<SCS>("help").executes{
            it.source.sendMessage(msg(it))
            1
        })
        return this
    }
}