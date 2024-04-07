package commandmaster.entityclient

import commandmaster.entity.CmdMastEntities
import commandmaster.entityclient.renderer.IntelligentZombieRenderer
import commandmaster.entityclient.renderer.MacroCreeperRenderer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.ZombieEntityRenderer

object CmdMastEntityRenderers {
    init{
        EntityRendererRegistry.register(CmdMastEntities.MACRO_CREEPER, ::MacroCreeperRenderer)
        EntityRendererRegistry.register(CmdMastEntities.SMART_ZOMBIE, ::IntelligentZombieRenderer)
    }
}