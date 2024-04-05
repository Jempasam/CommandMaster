package commandmaster.entityrenderer

import commandmaster.entity.CmdMastEntities
import io.netty.util.DefaultAttributeMap
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.mixin.`object`.builder.DefaultAttributeRegistryAccessor
import net.minecraft.client.render.entity.EntityRenderers
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.registry.Registries

object CmdMastEntityRenderers {
    init{
        EntityRendererRegistry.register(CmdMastEntities.MACRO_CREEPER,::MacroCreeperRenderer)
    }
}