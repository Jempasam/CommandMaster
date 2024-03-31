package commandmaster.components

import com.mojang.serialization.Codec
import commandmaster.CommandMaster
import commandmaster.macro.MacroCommand
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
    val MACRO_STATE= register<List<String>>("macro_state"){ codec(Codec.STRING.listOf()) }

    /**
     * Is shootable using the hand catapult
     */
    val IS_SHOOTABLE= register<UnitJ>("is_shootable"){ codec(Codec.unit(UnitJ.INSTANCE)) }

    private fun <T> register(id: String, op: DataComponentType.Builder<T>.()->Unit)=
        Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            CommandMaster/id,
            DataComponentType.builder<T>().also{it.op()}.build()
        ) as DataComponentType<T>
}