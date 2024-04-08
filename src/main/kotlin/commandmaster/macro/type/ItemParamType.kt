package commandmaster.macro.type

import commandmaster.macro.MacroParamType
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtIntArray
import net.minecraft.nbt.NbtString
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object ItemParamType: MacroParamType {

    override val name="item"

    override val example="minecraft:stick"

    override val color=0x6600AA

    override fun of(entity: Entity) = entity.asItemStack() ?.let {of(it)}

    override fun of(world: ServerWorld, pos: BlockPos) = null

    override fun of(stack: ItemStack): String?{
        if(stack.isEmpty)return null
        val item=stack.item
        return Registries.ITEM.getId(item.asItem())?.toString()
    }

    override fun of(text: String) = text.takeIf { Identifier.tryParse(text)?.let { Registries.BLOCK.containsId(it) } ?: false }

    override fun of(nbt: NbtElement): String?{
        if(nbt is NbtString) return of(nbt.asString())
        return null
    }
}