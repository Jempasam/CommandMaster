package commandmaster.macro.type

import commandmaster.macro.MacroParamType
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import kotlin.math.round

class BlockposParamType(suffix:String, val stringifier:(BlockPos)->String): MacroParamType {

    override val name="blockpos$suffix"

    override val example=stringifier(BlockPos.ORIGIN)

    override val color=0xAA2222

    override fun of(entity: Entity) = stringifier(entity.blockPos)

    override fun of(block: CachedBlockPosition) = stringifier(block.blockPos)

    override fun of(stack: ItemStack) = stack.getTarget() ?.let(stringifier)

    override fun of(text: String) = null

    override fun of(nbt: NbtElement): String? {
        if (nbt is NbtList && nbt.size == 3){
            if (nbt.heldType == NbtElement.INT_TYPE) {
                return stringifier(BlockPos(nbt.getInt(0),nbt.getInt(1),nbt.getInt(2)))
            }
        }
        else if(nbt is NbtCompound && nbt.size==3){
            val x=(nbt.get("x") as? AbstractNbtNumber)?.intValue() ?: return null
            val y=(nbt.get("y") as? AbstractNbtNumber)?.intValue() ?: return null
            val z=(nbt.get("z") as? AbstractNbtNumber)?.intValue() ?: return null
            return stringifier(BlockPos(x,y,z))
        }
        return null
    }

    companion object {
        val ALL=BlockposParamType(""){ "${it.x} ${it.y} ${it.z}" }
        val X=BlockposParamType("X"){ "${it.x}" }
        val Y=BlockposParamType("Y"){ "${it.y}" }
        val Z=BlockposParamType("Z"){ "${it.z}" }
    }
}