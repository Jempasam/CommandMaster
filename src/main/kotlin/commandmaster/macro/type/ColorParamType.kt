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

object ColorParamType: MacroParamType {

    override val name="color"

    override val example=DyeColor.RED.fireworkColor.toString()

    override val color=0xFFAAAA

    override fun of(entity: Entity) = entity.color.toString()

    override fun of(block: CachedBlockPosition) = block.blockState.block.color.toString()

    override fun of(stack: ItemStack) = if(stack.isEmpty) stack.item.color.toString() else null

    override fun of(text: String) = runCatching { Formatting.valueOf(text.uppercase()) }.getOrNull() ?.takeIf { it.isColor } ?.colorValue ?.toString()

    override fun of(nbt: NbtElement): String?{
        if(nbt is NbtString) return of(nbt.asString())
        else if(nbt is NbtInt) return nbt.intValue().toString()
        return null
    }
}