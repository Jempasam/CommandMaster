package commandmaster.entityclient.renderer

import commandmaster.CommandMaster
import commandmaster.entity.MacroCreeperEntity
import commandmaster.entityclient.model.ColoredDecoratorModel
import commandmaster.vertex.provider.colored
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.CreeperEntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer
import net.minecraft.client.render.entity.model.CreeperEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.mob.CreeperEntity
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.MathHelper
import kotlin.math.min
import kotlin.math.sin

class MacroCreeperRenderer(context: EntityRendererFactory.Context): CreeperEntityRenderer(context){

    override fun getTexture(entity: CreeperEntity)= CommandMaster/"textures/entity/macro_creeper.png"

    override fun render(mobEntity: CreeperEntity, f: Float, g: Float, matrixStack: MatrixStack, provider: VertexConsumerProvider, i: Int) {
        /*val red= min(1f, ColorHelper.Argb.getRed(mobEntity.color)/255f+g)
        val green= min(1f, ColorHelper.Argb.getGreen(mobEntity.color)/255f+g)
        val blue= min(1f, ColorHelper.Argb.getBlue(mobEntity.color)/255f+g)*/
        val macro=mobEntity as MacroCreeperEntity
        val red= ColorHelper.Argb.getRed(mobEntity.color)/255f
        val green= ColorHelper.Argb.getGreen(mobEntity.color)/255f
        val blue= ColorHelper.Argb.getBlue(mobEntity.color)/255f
        super.render(mobEntity, f, g, matrixStack, provider.colored(red,green,blue,1f), i)
    }
}