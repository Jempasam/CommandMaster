package commandmaster.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.*
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EnumArgumentType
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.command.argument.NbtPathArgumentType
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.util.StringIdentifiable.EnumCodec
import net.minecraft.util.dynamic.Codecs
import net.minecraft.server.command.ServerCommandSource as SCS

object SimulateCommands: CommandRegistrationCallback {
    override fun register(dispatcher: CommandDispatcher<SCS>, registryAccess: CommandRegistryAccess, environment: CommandManager.RegistrationEnvironment) {
        /*
        // Items useds
        val itemStackUsed= argument<SCS,_>("used", ItemStackArgumentType.itemStack(registryAccess))
        val itemStackGot= argument<SCS,_>("got", ItemStackArgumentType.itemStack(registryAccess))

        // Storage target
        val storage= argument<SCS,_>("storage", IdentifierArgumentType.identifier())
        val path= argument<SCS,_>("path", NbtPathArgumentType.nbtPath())

        fun writeInto(stack:ItemStack, context: CommandContext<SCS>){
            val storage=IdentifierArgumentType.getIdentifier(context, "storage")
            val path=NbtPathArgumentType.getNbtPath(context, "path")
            val storageData=context.source.server.dataCommandStorage.get(storage)
            path.put(storageData, stack.encode(registryAccess))
        }

        val useInAir= literal<SCS>("use").then(

        )*/
    }
}