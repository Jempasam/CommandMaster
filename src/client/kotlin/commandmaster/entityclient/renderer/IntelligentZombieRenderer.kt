package commandmaster.entityclient.renderer

import commandmaster.CommandMaster
import commandmaster.vertex.provider.colored
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer
import net.minecraft.client.render.entity.ZombieEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.EntityModelLayers.*
import net.minecraft.client.render.entity.model.ZombieEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.util.math.ColorHelper.Argb

class IntelligentZombieRenderer(context: EntityRendererFactory.Context): ZombieEntityRenderer(context)
{
    override fun getTexture(zombieEntity: ZombieEntity) = CommandMaster/"textures/entity/intelligent_zombie.png"

    override fun render(mobEntity: ZombieEntity, f: Float, g: Float, matrixes: MatrixStack, vertexs: VertexConsumerProvider, i: Int) {
        val color= mobEntity.teamColorValue
        val red= Argb.getRed(color)/255f
        val green= Argb.getGreen(color)/255f
        val blue= Argb.getBlue(color)/255f
        super.render(mobEntity, f, g, matrixes, vertexs.colored(red,green,blue,1f), i)
    }
}