package commandmaster.vertex.provider

import commandmaster.vertex.consumer.colored
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider

class ColoredVertexProvider(val decorated: VertexConsumerProvider, val red:Float, val green:Float, val blue:Float, val alpha:Float): VertexConsumerProvider by decorated {
    override fun getBuffer(layer: RenderLayer) = decorated.getBuffer(layer).colored(red,green,blue,alpha)
}

fun VertexConsumerProvider.colored(red:Float, green: Float, blue: Float, alpha: Float) = ColoredVertexProvider(this,red,green,blue,alpha)
