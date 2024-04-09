package commandmaster.macro.type

import commandmaster.macro.MacroParamType
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtElement.*
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class PositionParamType(suffix:String, val stringifier:(Vec3d)->String): MacroParamType {
    override val name="position$suffix"

    override val example=stringifier(Vec3d.ZERO)

    override val color=0xFF0000

    override fun of(entity: Entity) = stringifier(entity.pos)

    override fun of(block: CachedBlockPosition) = stringifier(Vec3d.ofCenter(block.blockPos))

    override fun of(stack: ItemStack) = stack.getTarget() ?.let{stringifier(Vec3d.ofCenter(it))}

    override fun of(text: String) = null

    override fun of(nbt: NbtElement): String? {
        if (nbt is NbtList && nbt.size == 3){
            if(nbt.heldType==FLOAT_TYPE)return stringifier(Vec3d(nbt.getFloat(0).toDouble(), nbt.getFloat(1).toDouble(), nbt.getFloat(2).toDouble()))
            else if(nbt.heldType==DOUBLE_TYPE)return stringifier(Vec3d(nbt.getDouble(0), nbt.getDouble(1), nbt.getDouble(2)))
        }
        else if(nbt is NbtCompound && nbt.size==3){
            val x=(nbt.get("x") as? AbstractNbtNumber)?.doubleValue() ?: return null
            val y=(nbt.get("y") as? AbstractNbtNumber)?.doubleValue() ?: return null
            val z=(nbt.get("z") as? AbstractNbtNumber)?.doubleValue() ?: return null
            return stringifier(Vec3d(x,y,z))
        }
        return null
    }

    companion object {
        val ALL=PositionParamType(""){ "${it.x} ${it.y} ${it.z}" }
        val X=PositionParamType("X"){ "${it.x}" }
        val Y=PositionParamType("Y"){ "${it.y}" }
        val Z=PositionParamType("Z"){ "${it.z}" }
    }
}