package commandmaster.macro

import commandmaster.macro.type.*
import commandmaster.utils.biMapOf
import net.minecraft.block.BlockState
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtElement
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

interface MacroParamType{

    val name: String

    val example: String

    val color: Int

    fun of(entity: Entity): String?
    fun of(block: CachedBlockPosition): String?
    fun of(stack: ItemStack): String?
    fun of(text: String): String?
    fun of(nbt: NbtElement): String?

    companion object{
        val TYPES= biMapOf<_,MacroParamType>(
            "p" to PositionParamType.ALL,
            "xp" to PositionParamType.X,
            "yp" to PositionParamType.Y,
            "zp" to PositionParamType.Z,

            "q" to BlockposParamType.ALL,
            "xq" to BlockposParamType.X,
            "yq" to BlockposParamType.Y,
            "zq" to BlockposParamType.Z,

            "d" to DirectionParamType.ALL,
            "xd" to DirectionParamType.YAW,
            "yd" to DirectionParamType.PITCH,

            "s" to SelectorParamType,
            "b" to BlockParamType,
            "i" to ItemParamType,
            "c" to ColorParamType,
            "t" to StringParamType
        )
    }
}
