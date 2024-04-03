package commandmaster.macro

import commandmaster.utils.biMapOf
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import java.util.UUID
import kotlin.math.round

interface MacroParamType{

    val name: String

    val example: String

    val color: Int

    fun selectAir(player: LivingEntity): String

    fun selectBlock(player: LivingEntity, world: ServerWorld, pos: BlockPos): String

    fun selectEntity(player: LivingEntity, entity: Entity): String

    object POSITION: MacroParamType{
        override val name="position"
        override val example="0 0 0"
        override val color=0xFF0000
        override fun selectAir(player: LivingEntity) = "${round(player.pos.x).toInt()} ${round(player.pos.y).toInt()} ${round(player.pos.z).toInt()}"
        override fun selectBlock(player: LivingEntity, world: ServerWorld, pos: BlockPos) = "${pos.x} ${pos.y} ${pos.z}"
        override fun selectEntity(player: LivingEntity, entity: Entity) = "${round(entity.pos.x).toInt()} ${round(entity.pos.y).toInt()} ${round(entity.pos.z).toInt()}"
    }

    object SELECTOR: MacroParamType{
        override val name="selector"
        override val example=UUID.randomUUID().toString()
        override val color=0x0000FF
        override fun selectAir(player: LivingEntity) = player.uuidAsString
        override fun selectBlock(player: LivingEntity, world: ServerWorld, pos: BlockPos) = player.uuidAsString
        override fun selectEntity(player: LivingEntity, entity: Entity) = entity.uuidAsString
    }

    object DIRECTION: MacroParamType{
        override val name="direction"
        override val example="0 0"
        override val color=0x00FF00
        override fun selectAir(player: LivingEntity) = "${player.pitch} ${player.yaw}"
        override fun selectBlock(player: LivingEntity, world: ServerWorld, pos: BlockPos) = "${player.pitch} ${player.yaw}"
        override fun selectEntity(player: LivingEntity, entity: Entity) = "${player.pitch} ${player.yaw}"
    }

    object BLOCK: MacroParamType{
        override val name="block"
        override val example="minecraft:stone"
        override val color=0xAA6600
        override fun selectAir(player: LivingEntity): String{
            val item=player.offHandStack.item
            if(item is BlockItem)return Registries.BLOCK.getId(item.block).toString()
            return "minecraft:air"
        }

        override fun selectBlock(player: LivingEntity, world: ServerWorld, pos: BlockPos)
                = Registries.BLOCK.getId(world.getBlockState(pos).block).toString()

        override fun selectEntity(player: LivingEntity, entity: Entity): String{
            if(entity !is LivingEntity)return "minecraft:air"
            val item=entity.mainHandStack.item
            if(item is BlockItem)return Registries.BLOCK.getId(item.block).toString()
            val item2=entity.offHandStack.item
            if(item2 is BlockItem)return Registries.BLOCK.getId(item2.block).toString()
            return "minecraft:air"
        }
    }

    object ITEM: MacroParamType{
        override val name="item"
        override val example="minecraft:stick"
        override val color=0x6600AA
        override fun selectAir(player: LivingEntity): String{
            val item=player.offHandStack.item
            return Registries.ITEM.getId(item).toString()
        }

        override fun selectBlock(player: LivingEntity, world: ServerWorld, pos: BlockPos): String{
            val block=world.getBlockState(pos).block
            if(block is BlockItem)return Registries.ITEM.getId(block).toString()
            return "minecraft:air"
        }

        override fun selectEntity(player: LivingEntity, entity: Entity): String{
            if(entity !is LivingEntity)return "minecraft:air"
            val item=entity.mainHandStack.item
            if(item!== Items.AIR)return Registries.ITEM.getId(item).toString()
            val item2=entity.offHandStack.item
            if(item2!== Items.AIR)return Registries.ITEM.getId(item2).toString()
            return "minecraft:air"
        }
    }

    companion object{
        val TYPES= biMapOf(
            'p' to POSITION,
            's' to SELECTOR,
            'd' to DIRECTION,
            'b' to BLOCK,
            'i' to ITEM
        )
    }
}
