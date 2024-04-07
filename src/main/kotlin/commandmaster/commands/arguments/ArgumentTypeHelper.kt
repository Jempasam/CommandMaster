package commandmaster.commands.arguments

import net.minecraft.entity.Entity
import net.minecraft.screen.ScreenTexts
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d

object ArgumentTypeHelper {
    var NULL_COMMAND_SOURCE = ServerCommandSource(
        CommandOutput.DUMMY, Vec3d.ZERO, Vec2f.ZERO, null,
        0, "Server", ScreenTexts.EMPTY, null as MinecraftServer?, null
    )
}