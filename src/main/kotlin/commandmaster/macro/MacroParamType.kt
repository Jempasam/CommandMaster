package commandmaster.macro

import commandmaster.utils.biMapOf
import net.minecraft.block.AnvilBlock
import net.minecraft.block.EnchantingTableBlock
import net.minecraft.block.entity.EnchantingTableBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

interface MacroParamType{

    val name: String

    val color: Int

    fun selectAir(player: ServerPlayerEntity): String

    fun selectBlock(player: ServerPlayerEntity, world: ServerWorld, pos: BlockPos): String

    fun selectEntity(player: ServerPlayerEntity, entity: Entity): String

    object POSITION: MacroParamType{
        override val name="position"
        override val color=0xFF0000
        override fun selectAir(player: ServerPlayerEntity) = "${player.pos.x.toInt()} ${player.pos.y.toInt()} ${player.pos.z.toInt()}"
        override fun selectBlock(player: ServerPlayerEntity, world: ServerWorld, pos: BlockPos) = "${pos.x} ${pos.y} ${pos.z}"
        override fun selectEntity(player: ServerPlayerEntity, entity: Entity) = "${player.pos.x.toInt()} ${player.pos.y.toInt()} ${player.pos.z.toInt()}"
    }

    object SELECTOR: MacroParamType{
        override val name="selector"
        override val color=0x0000FF
        override fun selectAir(player: ServerPlayerEntity) = "${player.uuidAsString}"
        override fun selectBlock(player: ServerPlayerEntity, world: ServerWorld, pos: BlockPos) = "${player.uuidAsString}"
        override fun selectEntity(player: ServerPlayerEntity, entity: Entity) = entity.uuidAsString
    }

    object DIRECTION: MacroParamType{
        override val name="direction"
        override val color=0x00FF00
        override fun selectAir(player: ServerPlayerEntity) = "~ ~"
        override fun selectBlock(player: ServerPlayerEntity, world: ServerWorld, pos: BlockPos) = "${player.pitch} ${player.yaw}"
        override fun selectEntity(player: ServerPlayerEntity, entity: Entity) = "${player.pitch} ${player.yaw}"
    }

    object BLOCK: MacroParamType{
        override val name="block"
        override val color=0xAA6600
        override fun selectAir(player: ServerPlayerEntity): String{
            val item=player.offHandStack.item
            if(item is BlockItem)return Registries.BLOCK.getId(item.block).toString()
            return "minecraft:air"
        }

        override fun selectBlock(player: ServerPlayerEntity, world: ServerWorld, pos: BlockPos)
                = Registries.BLOCK.getId(world.getBlockState(pos).block).toString()

        override fun selectEntity(player: ServerPlayerEntity, entity: Entity): String{
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
        override val color=0x6600AA
        override fun selectAir(player: ServerPlayerEntity): String{
            val item=player.offHandStack.item
            return Registries.ITEM.getId(item).toString()
        }

        override fun selectBlock(player: ServerPlayerEntity, world: ServerWorld, pos: BlockPos): String{
            val block=world.getBlockState(pos).block
            if(block is BlockItem)return Registries.ITEM.getId(block).toString()
            return "minecraft:air"
        }

        override fun selectEntity(player: ServerPlayerEntity, entity: Entity): String{
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
