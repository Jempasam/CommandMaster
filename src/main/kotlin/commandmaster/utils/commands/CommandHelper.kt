package commandmaster.utils.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

fun<T : ArgumentBuilder<ServerCommandSource, T>> T.help(msg:(CommandContext<ServerCommandSource>)-> Text): T{
    this.then(LiteralArgumentBuilder.literal<ServerCommandSource>("help").executes{
        it.source.sendMessage(msg(it))
        1
    })
    return this
}