package commandmaster.vertex.consumer

import net.minecraft.client.render.VertexConsumer

class ColoredVertexConsumer(val decorated: VertexConsumer, val red:Float, val green:Float, val blue:Float, val alpha:Float): VertexConsumer by decorated {
    override fun color(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer {
        return decorated.color((red*this.red).toInt(), (green*this.green).toInt(), (blue*this.blue).toInt(), (alpha*this.alpha).toInt())
    }
}

fun VertexConsumer.colored(red:Float, green: Float, blue: Float, alpha: Float) = ColoredVertexConsumer(this,red,green,blue,alpha)
