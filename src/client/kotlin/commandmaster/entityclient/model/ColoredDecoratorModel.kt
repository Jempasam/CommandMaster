package commandmaster.entityclient.model

import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity

class ColoredDecoratorModel<T : Entity>(val sub: EntityModel<T>, var red: Float, var blue: Float, var green: Float):  EntityModel<T>(){
    override fun render(matrices: MatrixStack, vertices: VertexConsumer, light: Int, overlay: Int, red: Float, green: Float, blue: Float, alpha: Float) {
        sub.render(matrices, vertices, light, overlay, this.red*red, this.green*green, this.blue*blue, alpha)
    }

    override fun setAngles(entity: T, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) {
        sub.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch)
    }

}