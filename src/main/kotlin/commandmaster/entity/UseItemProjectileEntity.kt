package commandmaster.entity

import com.jcraft.jorbis.Block
import commandmaster.utils.entity.dataTracked
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import slabmagic.simulator.PlayerSimulator

class UseItemProjectileEntity(entityType: EntityType<out UseItemProjectileEntity>, world: World): ThrownItemEntity(entityType, world) {

    override fun getDefaultItem() = Items.APPLE

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        val entity = entityHitResult.entity
        if(entity is LivingEntity){
            val player=PlayerSimulator(world,blockPos)
            player.yaw=yaw
            player.pitch=pitch
            player.setPosition(entityHitResult.pos)
            player.setStackInHand(Hand.MAIN_HAND,stack)
            val result=stack.useOnEntity(player,entity, Hand.MAIN_HAND)
        }
        discard()
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        println("aaaa")
        val player=PlayerSimulator(world,blockPos)
        player.yaw=yaw
        player.pitch=pitch
        player.setPosition(blockHitResult.pos)
        player.setStackInHand(Hand.MAIN_HAND,stack)
        val result=stack.useOnBlock(ItemUsageContext(player,Hand.MAIN_HAND, blockHitResult))
        discard()
    }
}