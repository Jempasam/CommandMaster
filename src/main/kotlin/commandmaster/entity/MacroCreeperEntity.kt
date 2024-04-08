package commandmaster.entity

import commandmaster.entity.trait.MacroHolder
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroCompletion
import commandmaster.utils.entity.dataTracked
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.CreeperEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.DyeColor
import net.minecraft.world.World

class MacroCreeperEntity(entityType: EntityType<out MacroCreeperEntity>, world: World) : CreeperEntity(entityType, world), MacroHolder {

    override var macro=MacroCommand("")

    var color by dataTracked(COLOR)

    override fun explode() {
        if (!world.isClient) {
            macro.build(MacroCompletion()).onSuccess{ command->
                val server=world.server
                if(server!=null){
                    MacroCommand.executeMultiline(server,commandSource.withLevel(2).withSilent(),command)
                }
            }
            this.dead = true
            this.discard()
        }
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        nbt.getString("macro") ?.let { macro = MacroCommand(it) }
        nbt.getInt("color") .takeIf {it!=0} ?.let { color = it }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putString("macro", macro.command)
        nbt.putInt("color", color)
    }

    override fun initDataTracker(builder: DataTracker.Builder) {
        super.initDataTracker(builder)
        builder.add(COLOR, DyeColor.WHITE.fireworkColor)
    }

    companion object{
        val COLOR = DataTracker.registerData(MacroCreeperEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
    }

}