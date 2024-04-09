package commandmaster.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.*
import com.mojang.brigadier.builder.RequiredArgumentBuilder.*
import com.mojang.brigadier.context.CommandContext
import commandmaster.blockentity.FullComponentBlockEntity
import commandmaster.commands.arguments.MacroCommandArgumentType
import commandmaster.commands.arguments.MultiCommandArgumentType
import commandmaster.components.CmdMastComponents
import commandmaster.enchantments.CmdMastEnchantments
import commandmaster.files.FileSystem
import commandmaster.item.CmdMastItems
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroCompletion
import commandmaster.macro.MacroParamType
import commandmaster.network.NbtFetcher
import commandmaster.utils.commands.help
import commandmaster.utils.nbt.toText
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.block.entity.CommandBlockBlockEntity
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.command.argument.NbtElementArgumentType
import net.minecraft.command.argument.NbtPathArgumentType
import net.minecraft.component.Component
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.command.ServerCommandSource as SCS
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.net.URI
import java.nio.charset.Charset
import kotlin.jvm.optionals.getOrNull


object CmdMastCommands {
    init{
        CommandRegistrationCallback.EVENT.register(MacroCommands)
        CommandRegistrationCallback.EVENT.register{ disp, reg, man ->

            // Action that give item


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

            fun<T> give_do(context: CommandContext<SCS>, getter: ()->T?, of: MacroParamType.(T)->String?): Int{
                val player=context.source.player
                if(player==null){
                    context.source.sendError(Text.of("Need Player!"))
                    return 0
                }

                var stack=player.mainHandStack
                if(stack.isEmpty || stack.item!=CmdMastItems.COMMAND_WAND)stack=player.offHandStack
                if(stack.isEmpty || stack.item!=CmdMastItems.COMMAND_WAND){
                    context.source.sendError(Text.of("Need a wand in hand!"))
                    return 0
                }

                val macro=stack.get(CmdMastComponents.MACRO_HOLDER)
                if(macro==null) {
                    context.source.sendError(Text.of("Need a macro in the wand!"))
                    return 0
                }

                val compo=stack.get(CmdMastComponents.MACRO_COMPLETION) ?: MacroCompletion()
                if(compo.size<macro.parameters.size){
                    val value=getter()
                    if(value==null){
                        context.source.sendError(Text.of("Invalid target, not found!"))
                        return 0
                    }
                    try{
                        stack.set(CmdMastComponents.MACRO_COMPLETION,compo.builder().add(value,of,macro).build())
                    }catch (e:Exception){
                        context.source.sendError(Text.of("Error: ${e.message}"))
                        return 0
                    }
                }
                return 1
            }

            val COMMAND= literal<SCS>("command").requires {it.hasPermissionLevel(2)}
                .then(EXAMPLE)
                .help {
                    val message=Text.literal("""
                            Create items that run macro commands.
                            Macro commands are normal commands with parameters.
                            The parameters can be one of the following:
                            """.trimIndent())
                    for((key,param) in MacroParamType.TYPES){
                        val value=it.source.player?.let { ", Example: "+param.of(it) } ?: ""
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
                            command= blockentity.get(CmdMastComponents.MACRO_HOLDER) ?.build(MacroCompletion()) ?.getOrNull() ?: break
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

            val REPEAT= literal<SCS>("repeat").then(
                argument<SCS,_>("count",IntegerArgumentType.integer(1)).fork(disp.root){ context ->
                    List(IntegerArgumentType.getInteger(context,"count")){context.source}
                }
            )

            val FETCHNBT=literal<SCS>("fetchnbt").then(
                argument<SCS,_>("uri",StringArgumentType.string()).then(
                    argument<SCS,_>("storage", IdentifierArgumentType.identifier()).then(
                        argument<SCS,_>("target",NbtPathArgumentType.nbtPath()).then(
                            argument<SCS,_>("callback", MacroCommandArgumentType).executes com@{
                                val path = StringArgumentType.getString(it, "uri")
                                val storage = IdentifierArgumentType.getIdentifier(it, "storage")
                                val target= NbtPathArgumentType.getNbtPath(it, "target")
                                val macro= it.getArgument("callback",String::class.java)

                                // Parse macro
                                val command=MacroCommand(macro).build(MacroCompletion())?.getOrNull() ?: run{it.source.sendError(Text.of("Invalid macro!")); return@com 0}

                                // Send
                                val uri= runCatching { URI.create(path) }.getOrNull() ?: run{it.source.sendError(Text.of("Invalid URI!")); return@com 0}
                                NbtFetcher.fetchNbt(uri).thenApply {result ->
                                    if(result.isFailure)it.source.sendError(Text.of("Fetch Error: ${result.exceptionOrNull()?.message}"))
                                    else{
                                        val storagedata=it.source.server.dataCommandStorage.get(storage)
                                        target.put(storagedata,result.getOrThrow())
                                        it.source.server.commandManager.executeWithPrefix(it.source,command)
                                        it.source.server.dataCommandStorage.set(storage,storagedata)
                                    }
                                }
                                1
                            }
                        )
                    )
                )
            ).help{
                Text.of("""
                    Fetch the nbt on internet and save it into a storage.
                    <uri> is the uri of the nbt file.
                    <storage> is the storage identifer to save the nbt.
                    <target> is the path to save the nbt in the storage.
                    <callback> is the macro to run after the nbt is fetched.
                    """)
            }

            disp.register(COMMAND)
            disp.register(FETCHNBT)
            disp.register(REPEAT)
            //disp.register(FILE)
            disp.register(RUNSTACK)
            disp.register(MULTI)
        }
    }
}