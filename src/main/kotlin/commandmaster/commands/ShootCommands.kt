package commandmaster.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType.getDouble
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.*
import com.mojang.brigadier.context.CommandContext
import commandmaster.entity.CmdMastEntities
import commandmaster.entity.UseItemProjectileEntity
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.AngleArgumentType
import net.minecraft.command.argument.AngleArgumentType.getAngle
import net.minecraft.command.argument.ItemStackArgument
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.command.argument.ItemStackArgumentType.*
import net.minecraft.command.argument.Vec2ArgumentType
import net.minecraft.command.argument.Vec2ArgumentType.getVec2
import net.minecraft.item.ItemStack
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.server.command.ServerCommandSource as SCS

object ShootCommands: CommandRegistrationCallback {
    override fun register(disp: CommandDispatcher<SCS>, registry: CommandRegistryAccess, env: CommandManager.RegistrationEnvironment) {

        fun shoot(source: CommandContext<SCS>, strength: Double, angle: Vec2f): Int{
            val stack = getItemStackArgument(source,"item").createStack(1,false)
            if(stack.isEmpty)return 0
            val entity= UseItemProjectileEntity(CmdMastEntities.ITEM_PROJECTILE, source.source.world)
            entity.setItem(stack)
            entity.setPosition(source.source.position)
            entity.owner=source.source.entity

            val velocity=Vec3d.fromPolar(angle).multiply(strength)
            entity.setVelocity(velocity.x,velocity.y,velocity.z)
            source.source.world.spawnEntity(entity)
            return 1
        }

        val shoot= literal<SCS>("shootitem").then(
            argument<SCS,_>("item", itemStack(registry)).then(
                argument<SCS,_>("strength", DoubleArgumentType.doubleArg(0.0)).then(
                    argument<SCS,_>("angle", Vec2ArgumentType.vec2()).executes{
                        shoot(it, getDouble(it,"strength"), getVec2(it,"angle"))
                    }
                ).executes{
                    shoot(it, getDouble(it,"strength"), it.source.rotation)
                }
            ).executes{
                shoot(it, 1.0, it.source.rotation)
            }
        )
        disp.register(shoot)

    }

}