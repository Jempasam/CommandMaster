package commandmaster.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.*
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.ColorHelper.Argb
import net.minecraft.util.math.ColorHelper.Argb.*
import org.apache.logging.log4j.core.util.Integers
import java.awt.image.RenderedImage
import javax.imageio.ImageIO
import kotlin.io.path.Path

object CmdMastClientCommands {

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>, registries: CommandRegistryAccess){
        val GET_COLOR=literal<FabricClientCommandSource>("get_color").executes{ context ->
            // Get Mouse pose
            val mx=MinecraftClient.getInstance().mouse.x/ MinecraftClient.getInstance().window.width.toDouble()
            val my=MinecraftClient.getInstance().mouse.y/ MinecraftClient.getInstance().window.height.toDouble()

            // Get Color
            val image= ScreenshotRecorder.takeScreenshot(MinecraftClient.getInstance().framebuffer)
            val ptx=(image.width*mx).toInt()
            val pty=(image.height*my).toInt()
            var color=Integer.reverseBytes(image.getColor(ptx,pty))
            color= getArgb(getBlue(color), getAlpha(color), getRed(color), getGreen(color))
            context.source.sendFeedback(Text.literal("Color: ").append(Text.literal("$color").withColor(color)))
            color
        }.help {
            Text.literal("Get the color of the pixel under the mouse")
        }

        dispatcher.register(GET_COLOR)
    }

    init {
        ClientCommandRegistrationCallback.EVENT.register(this::register)
    }

    private fun<T : ArgumentBuilder<FabricClientCommandSource, T>> T.help(msg:(CommandContext<FabricClientCommandSource>)->Text): T{
        this.then(literal<FabricClientCommandSource>("help").executes{
            it.source.sendFeedback(msg(it))
            1
        })
        return this
    }
}