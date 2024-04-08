package commandmaster.macro.type

import commandmaster.macro.MacroParamType
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtIntArray
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

object SelectorParamType: MacroParamType {

    override val name="selector"

    override val example="@s"

    override val color=0x0000FF

    override fun of(entity: Entity) = entity.uuidAsString

    override fun of(block: CachedBlockPosition) = null

    override fun of(stack: ItemStack) = null

    override fun of(text: String) = null

    override fun of(nbt: NbtElement): String?{
        if(nbt is NbtIntArray && nbt.size==4)return NbtHelper.toUuid(nbt).toString()
        return null
    }
}