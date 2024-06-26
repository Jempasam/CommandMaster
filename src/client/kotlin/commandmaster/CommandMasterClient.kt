package commandmaster

import commandmaster.block.CmdMastBlocks
import commandmaster.color.DyableBlockColorProvider
import commandmaster.color.DyableItemColorProvider
import commandmaster.commands.CmdMastClientCommands
import commandmaster.entityclient.CmdMastEntityRenderers
import commandmaster.item.CmdMastItems
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.Colors

object CommandMasterClient : ClientModInitializer {
	override fun onInitializeClient() {
		// Item Color
		ColorProviderRegistry.ITEM.register(DyableItemColorProvider(14915644), CmdMastItems.COMMAND_WAND)
		ColorProviderRegistry.ITEM.register(DyableItemColorProvider(Colors.WHITE), CmdMastItems.MACHINE_BLOCK)

		// Block Color
		ColorProviderRegistry.BLOCK.register(DyableBlockColorProvider, CmdMastBlocks.MACHINE_BLOCK)

		// BlockDisplay
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), CmdMastBlocks.MACHINE_BLOCK)

		// Entity Renderers
		CmdMastEntityRenderers

		CmdMastClientCommands
	}
}