package slabmagic.simulator

import com.mojang.authlib.GameProfile
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.UseAction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

class PlayerSimulator(world: World, pos: BlockPos): PlayerEntity(world, pos, 0f, GameProfile(UUID.randomUUID(),"herobrine"))
{
    override fun isSpectator() = false

    override fun isCreative() = false

    fun useOnBlock(stack: ItemStack, pos: BlockPos, rot: Vec2f): ActionResult{
        val position=Vec3d.ofCenter(pos)
        moveTo(position,rot)
        setStackInHand(Hand.MAIN_HAND,stack)
        return stack.item.useOnBlock(ItemUsageContext(this,Hand.MAIN_HAND, BlockHitResult(position,Direction.UP,pos,true)))
    }

    fun useOnEntity(stack: ItemStack, entity: LivingEntity, rot: Vec2f): ActionResult{
        moveTo(pos,rot)
        setStackInHand(Hand.MAIN_HAND,stack)
        val ret=stack.item.useOnEntity(stack,this,entity,Hand.MAIN_HAND)
        if(ret.isAccepted)return ret
        return entity.interact(this,Hand.MAIN_HAND)
    }

    fun use(stack: ItemStack, pos: Vec3d, rot: Vec2f): ActionResult{
        moveTo(pos,rot)
        setStackInHand(Hand.MAIN_HAND,stack)
        val result=stack.item.use(world,this, Hand.MAIN_HAND).result
        if (result.isAccepted && stack.item.getUseAction(stack)!=UseAction.NONE){
            stack.item.finishUsing(mainHandStack,world,this)
        }
        return result
    }

    private fun moveTo(pos: Vec3d, rot: Vec2f){
        setRotation(rot.y,rot.x)
        setPosition(pos.subtract(0.0, standingEyeHeight.toDouble(), 0.0))
    }
}