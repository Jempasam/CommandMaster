package commandmaster.macro.type

import commandmaster.macro.MacroParamType
import net.minecraft.block.AbstractSignBlock
import net.minecraft.block.Blocks
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtIntArray
import net.minecraft.nbt.NbtString
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.core.util.Integers

class IntParamType(val added: Int): MacroParamType {

    override val name=when{
        added<0 -> "int${added}"
        added>0 -> "int+${added}"
        else -> "int"
    }

    override val example="10"

    override val color=0x33FFFF

    override fun of(entity: Entity) = (entity as? PlayerEntity)?.offHandStack?.count?.let{it+added}?.toString()

    override fun of(block: CachedBlockPosition) = null

    override fun of(stack: ItemStack) = stack.count.let{it+added}.toString()

    override fun of(text: String) = runCatching{text.toInt()}.getOrNull()?.let{it+added}?.toString()

    override fun of(nbt: NbtElement) = nbt.let{nbt as? NbtInt} ?.intValue()?.let{it+added}.toString()
}