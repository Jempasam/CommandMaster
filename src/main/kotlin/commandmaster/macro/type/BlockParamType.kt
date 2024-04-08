package commandmaster.macro.type

import commandmaster.macro.MacroParamType
import net.minecraft.block.pattern.CachedBlockPosition
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

object BlockParamType: MacroParamType {

    override val name="block"

    override val example="minecraft:dirt"

    override val color=0xAA6600

    override fun of(entity: Entity) = entity.asItemStack() ?.let {of(it)}
    override fun of(block: CachedBlockPosition) = Registries.BLOCK.getId(block.blockState.block).toString()

    override fun of(stack: ItemStack): String?{
        if(stack.isEmpty)return null
        val item=stack.item
        if(item is BlockItem)return Registries.BLOCK.getId(item.block).toString()
        else return null
    }

    override fun of(text: String) = text.takeIf { Identifier.tryParse(text)?.let { Registries.BLOCK.containsId(it) } ?: false }

    override fun of(nbt: NbtElement): String?{
        if(nbt is NbtString) return of(nbt.asString())
        return null
    }
}