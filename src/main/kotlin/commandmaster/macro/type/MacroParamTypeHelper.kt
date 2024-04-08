package commandmaster.macro.type

import net.minecraft.block.Block
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.DyeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.SpawnEggItem
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d


val ItemStack.notEmpty get() = this.takeIf { !it.isEmpty }

fun Entity.asItemStack(): ItemStack?{
    if(this is LivingEntity) return offHandStack.notEmpty ?: mainHandStack.notEmpty
    else if(this is ItemFrameEntity) return heldItemStack.notEmpty
    else if(this is ItemEntity) return stack.notEmpty
    return null
}

val Entity.color get() = SpawnEggItem.forEntity(type)?.let { it.getColor(0) }
        ?: teamColorValue.takeIf { scoreboardTeam!=null }
        ?: DyeColor.RED.fireworkColor.takeIf { !this.type.spawnGroup.isPeaceful }
        ?: DyeColor.WHITE.fireworkColor

val Block.color get() = defaultMapColor.color

val Item.color get() = when(this){
    is SpawnEggItem -> this.getColor(0)
    is DyeItem -> this.color.fireworkColor
    is BlockItem -> this.block.color
    else -> DyeColor.WHITE.fireworkColor
}

fun ItemStack.getTarget(): BlockPos?{
    run{
        val tracker=get(DataComponentTypes.LODESTONE_TRACKER)
        if(tracker!=null && tracker.target.isPresent)return tracker.target.get().pos
    }
    return null
}