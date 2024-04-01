package commandmaster.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.*
import com.mojang.brigadier.builder.RequiredArgumentBuilder.*
import com.mojang.brigadier.context.CommandContext
import commandmaster.blockentity.FullComponentBlockEntity
import commandmaster.components.CmdMastComponents
import commandmaster.files.FileSystem
import commandmaster.item.CmdMastItems
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroParamType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.block.CommandBlock
import net.minecraft.block.entity.CommandBlockBlockEntity
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.BlockStateArgument
import net.minecraft.command.argument.BlockStateArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.item.BookItem
import net.minecraft.item.WritableBookItem
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.command.SetBlockCommand
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.math.BlockPos
import java.nio.charset.Charset
import kotlin.math.max

object CmdMastCommands {
    init{
        CommandRegistrationCallback.EVENT.register{ disp, reg, man ->

            // Action that give item
            val WAND=literal<ServerCommandSource>("wand").then(
                argument<ServerCommandSource,_>("macro",MacroCommandArgumentType).executes{
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
            val ITEM=literal<ServerCommandSource>("item").then(
                argument<ServerCommandSource,_>("item",ItemStackArgumentType.itemStack(reg)).then(
                    argument<ServerCommandSource,_>("macro",MacroCommandArgumentType).executes{
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
            val MACHINE=literal<ServerCommandSource>("machine").then(
                argument<ServerCommandSource,_>("macro",MacroCommandArgumentType).executes{
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
            val SHOW=literal<ServerCommandSource>("show").then(
                argument<ServerCommandSource,_>("macro",MacroCommandArgumentType).executes{
                    val macro=MacroCommand(it.getArgument("macro",String::class.java))
                    it.source.sendMessage(macro.text)
                    macro.parameters.size
                }
            )

            val COMMAND= literal<ServerCommandSource>("command").requires {it.hasPermissionLevel(2)}
                .then(WAND)
                .then(SHOW)
                .then(MACHINE)
                .then(ITEM)
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

            val FILE= literal<ServerCommandSource>("file").requires {it.hasPermissionLevel(2)}.then(
                literal<ServerCommandSource>("exist").then(
                    argument<ServerCommandSource,_>("path",StringArgumentType.string()).executes{
                        FileSystem(it.source.server).exist(FileSystem.getPath("path",it)).let { if(it) 1 else 0 }
                    }
                )
            ).then(
                literal<ServerCommandSource>("list").then(
                    argument<ServerCommandSource,_>("path",StringArgumentType.string()).executes{
                        val content=FileSystem(it.source.server).list(FileSystem.getPath("path",it))
                        it.source.sendMessage(Text.of("\nDIRECTORIES:\n"+content.joinToString("\n - ")))
                        1
                    }
                )
            ).then(
                literal<ServerCommandSource>("tree").then(
                    argument<ServerCommandSource,_>("path",StringArgumentType.string()).executes{
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
                literal<ServerCommandSource>("mkdir").then(
                    argument<ServerCommandSource,_>("path",StringArgumentType.string()).executes{
                        FileSystem(it.source.server).mkdir(FileSystem.getPath("path",it)).let { if(it) 1 else 0 }
                    }
                )
            ).then(
                literal<ServerCommandSource>("write").then(
                    argument<ServerCommandSource,_>("path",StringArgumentType.string()).then(
                        argument<ServerCommandSource,_>("content",StringArgumentType.string()).executes{
                            val content=it.getArgument("content",String::class.java).replace("\$LR$","\n").byteInputStream()
                            val path=FileSystem(it.source.server).write(FileSystem.getPath("path",it),content)
                            1
                        }
                    )
                )
            ).then(
                literal<ServerCommandSource>("read").then(
                    argument<ServerCommandSource,_>("path",StringArgumentType.string()).executes exec@{ context->
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

            val RUNSTACK=literal<ServerCommandSource>("runstack").then(
                argument<ServerCommandSource,_>("position",BlockPosArgumentType.blockPos()).executes{
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

            val MULTI=literal<ServerCommandSource>("multi").then(
                argument<ServerCommandSource,_>("commands",MultiCommandArgumentType).executes{
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
            disp.register(FILE)
            disp.register(RUNSTACK)
            disp.register(MULTI)
        }
    }

    private fun<T : ArgumentBuilder<ServerCommandSource, T>> T.help(msg:(CommandContext<ServerCommandSource>)->Text): T{
        this.then(literal<ServerCommandSource>("help").executes{
            it.source.sendMessage(msg(it))
            1
        })
        return this
    }
}