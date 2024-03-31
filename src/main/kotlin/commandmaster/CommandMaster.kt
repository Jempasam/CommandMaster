package commandmaster

import commandmaster.block.CmdMastBlocks
import commandmaster.blockentity.CmdMastBlockEntities
import commandmaster.commands.CmdMastArgumentTypes
import commandmaster.commands.CmdMastCommands
import commandmaster.components.CmdMastComponents
import commandmaster.group.CmdMastItemGroup
import commandmaster.item.CmdMastItems
import net.fabricmc.api.ModInitializer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object CommandMaster : ModInitializer {
	private val logger = LoggerFactory.getLogger("commandmaster")

	override fun onInitialize() {
		logger.info("Command Master Init!")
		CmdMastBlocks
		CmdMastComponents
		CmdMastItems
		CmdMastCommands
		CmdMastBlockEntities
		CmdMastItemGroup
		CmdMastArgumentTypes
	}

	/* MODID */
	const val MODID = "commandmaster"

	operator fun div(id: String) = Identifier(MODID, id)

	fun i18n(prefix: String, id: String) = (this / id).toTranslationKey(prefix)

	fun translatable(prefix: String, id: String, vararg obj: Any) = Text.translatable(i18n(prefix, id), *obj)
}