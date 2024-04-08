package commandmaster.macro.type

import commandmaster.macro.MacroParamType
import net.minecraft.block.Blocks
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ItemFrameEntity
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

object StringParamType: MacroParamType {

    override val name="text"

    override val example="Hello guy!"

    override val color=0x77AAFF

    override fun of(entity: Entity) = entity.name.toString()

    override fun of(block: CachedBlockPosition) = block.blockState.block.name.toString()

    override fun of(stack: ItemStack) = stack.name.toString()

    override fun of(text: String) = text

    override fun of(nbt: NbtElement) = nbt.let{nbt as? NbtString} ?.asString()
}