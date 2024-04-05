package commandmaster.entityrenderer

import commandmaster.CommandMaster
import commandmaster.entity.MacroCreeperEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.CreeperEntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.model.CreeperEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.MathHelper

class MacroCreeperRenderer(context: EntityRendererFactory.Context)
    : MobEntityRenderer<MacroCreeperEntity,ColoredDecoratorModel<MacroCreeperEntity>>(
        context,
        ColoredDecoratorModel(CreeperEntityModel(context.getPart(EntityModelLayers.CREEPER)),1f,1f,1f),
        0.5f
    ){
    override fun getTexture(entity: MacroCreeperEntity)= CommandMaster/"textures/entity/macro_creeper.png"

    override fun render(mobEntity: MacroCreeperEntity, f: Float, g: Float, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, i: Int
    ) {
        this.model.red=ColorHelper.Argb.getRed(mobEntity.color)/255f
        this.model.green=ColorHelper.Argb.getGreen(mobEntity.color)/255f
        this.model.blue=ColorHelper.Argb.getBlue(mobEntity.color)/255f
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i)
    }

    override fun scale(entity: MacroCreeperEntity, matrices: MatrixStack, amount: Float) {
        var g = entity.getClientFuseTime(amount)
        val h = 1.0f + MathHelper.sin(g * 100.0f) * g * 0.01f
        g = MathHelper.clamp(g, 0.0f, 1.0f)
        g *= g
        g *= g
        val i = (1.0f + g * 0.4f) * h
        val j = (1.0f + g * 0.1f) / h
        matrices.scale(i, j, i)
    }
}