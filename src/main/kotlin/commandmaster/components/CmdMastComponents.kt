package commandmaster.components

import com.mojang.serialization.Codec
import commandmaster.CommandMaster
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroCompletion
import net.minecraft.component.DataComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Unit as UnitJ

object CmdMastComponents {

    /**
     * Store a macro command.
     */
    val MACRO_HOLDER= register<MacroCommand>("macro_holder"){ codec(MacroCommand.CODEC) }

    /**
     * Store a macro command execution state.
     */
    val MACRO_COMPLETION= register<MacroCompletion>("macro_completion"){ codec(MacroCompletion.CODEC) }

    /**
     * Is shootable using the hand catapult
     */
    val IS_SHOOTABLE= register<UnitJ>("is_shootable"){ codec(Codec.unit(UnitJ.INSTANCE)) }

    /**
     * Upgrader component, store the target and the components to apply.
     * An upgrader contains components to apply to an item when it is upgraded.
     */
    val UPGRADER_COMPONENT= register<UpgraderComponent>("upgrader_component"){ codec(UpgraderComponent.CODEC) }

    private fun <T> register(id: String, op: DataComponentType.Builder<T>.()->Unit)=
        Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            CommandMaster/id,
            DataComponentType.builder<T>().also{it.op()}.build()
        ) as DataComponentType<T>
}