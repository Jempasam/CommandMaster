package commandmaster.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.*
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.*
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.BlockPosArgumentType.*
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.RegistrationEnvironment
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.min
import net.minecraft.server.command.ServerCommandSource as SCS

object CmdMastExecuteCommands: CommandRegistrationCallback {
    override fun register(disp: CommandDispatcher<SCS>, registry: CommandRegistryAccess, env: RegistrationEnvironment) {

        /**
         * Repeat a command a number of time
         */
        val REPEAT= literal<SCS>("repeat").then(
            argument<SCS, _>("count", integer(1)).fork(disp.root.getChild("execute")){ context ->
                List(getInteger(context,"count")){context.source}
            }
        )

        /**
         * Repeat a command for each block in a region
         */
        fun forEachBlock(source: SCS, from: BlockPos, to: BlockPos): Collection<SCS>{
            val mx= min(from.x,to.x); val my= min(from.y,to.y); val mz= min(from.z,to.z)
            val dx= abs(to.x-from.x)+1; val dy= abs(to.y-from.y)+1; val dz= abs(to.z-from.z)+1
            val count=dx*dy*dz
            return List(count){
                val x=it%dx +mx
                val y=(it/dx)%dy +my
                val z=it/(dx*dy) +mz
                source.withPosition(Vec3d(x+0.5,y+0.5,z+0.5))
            }
        }
        val EACH= literal<SCS>("each").then(
            argument<SCS,_>("from", blockPos()).then(
                argument<SCS,_>("to", blockPos()).fork(disp.root.getChild("execute")){ context ->
                    val from= getBlockPos(context,"from")
                    val to= getBlockPos(context,"to")
                    forEachBlock(context.source,from,to)
                }
            )
        )

        disp.root.getChild("execute").apply {
            addChild(REPEAT.build())
            addChild(EACH.build())
        }
    }
}