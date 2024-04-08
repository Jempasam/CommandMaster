package commandmaster.macro.type

import commandmaster.macro.MacroParamType
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtElement.*
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class DirectionParamType(val suffix:String, val stringifier: (Float,Float)->String): MacroParamType {

    override val name="direction$suffix"

    override val example=stringifier(0f,0f)

    override val color=0xFFFF00

    override fun of(entity: Entity) = stringifier(entity.yaw,entity.pitch)

    override fun of(world: ServerWorld, pos: BlockPos) = null

    override fun of(stack: ItemStack) = null

    override fun of(text: String) = null

    override fun of(nbt: NbtElement): String? {
        if (nbt is NbtList && nbt.size == 2){
            if(nbt.type== FLOAT_TYPE)return stringifier(nbt.getFloat(0), nbt.getFloat(1))
        }
        else if(nbt is NbtCompound && nbt.size==3){
            val pitch=(nbt.get("pitch") as? AbstractNbtNumber)?.floatValue() ?: return null
            val yaw=(nbt.get("yaw") as? AbstractNbtNumber)?.floatValue() ?: return null
            return stringifier(yaw,pitch)
        }
        return null
    }

    companion object{
        val ALL=DirectionParamType(""){yaw,pitch-> "$yaw $pitch"}
        val YAW=DirectionParamType("Yaw"){yaw,pitch-> "$yaw"}
        val PITCH=DirectionParamType("Pitch"){yaw,pitch-> "$pitch"}
    }
}