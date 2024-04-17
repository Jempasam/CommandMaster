package commandmaster.commands.arguments

import com.mojang.brigadier.arguments.ArgumentType
import commandmaster.CommandMaster
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.minecraft.command.argument.serialize.ArgumentSerializer

object CmdMastArgumentTypes {

    val MULTI_COMMAND = register("multi_command", MultiCommandArgumentType::class.java, MultiCommandArgumentType.Serializer)
    val MACRO_COMMAND = register("macro", MacroCommandArgumentType.javaClass, MacroCommandArgumentType.Serializer)

    fun <Y: ArgumentType<*>, T: ArgumentSerializer<Y,*>> register(id: String, clazz: Class<Y>, type: T): T{
        ArgumentTypeRegistry.registerArgumentType(CommandMaster/id, clazz, type)
        return type
    }
}