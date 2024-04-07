package commandmaster.entity.goal

import commandmaster.entity.trait.BuilderMob
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import java.util.EnumSet
import kotlin.math.abs
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1

class BridgeToTargetGoal<T>(val container: KMutableProperty1<T,ItemStack>, val actor:T): Goal() where T: HostileEntity, T: BuilderMob {

    private var actionTick=0

    init {
        controls=EnumSet.of(Control.TARGET)
    }

    private fun haveBlockUnder(): Boolean{
        val target= actor.target ?: return false

        // Is under empty
        val under_pos=BlockPos.ofFloored(actor.pos.subtract(0.0,0.3,0.0))
        if(target.world.getBlockState(under_pos).isAir && target.world.getBlockState(under_pos.down()).isAir)return true

        // Is at different height level
        if(abs(target.pos.y-actor.pos.y)>=2)return true
        return false
    }

    override fun canStart(): Boolean{
        if(container.get(actor).isEmpty)return false
        return haveBlockUnder()
    }

    override fun shouldContinue() = actionTick>0 || (haveBlockUnder() && !actor.mainHandStack.isEmpty)

    override fun start() {
        super.start()
        actionTick=0
        actor.equip(container,true)
    }

    override fun stop() {
        super.stop()
        actor.unequip(container,true)
    }

    override fun tick() {
        super.tick()

        val target=actor.target

        // Build under
        val under_pos=BlockPos.ofFloored(actor.pos.subtract(0.0,0.3,0.0))
        val under=actor.world.getBlockState(under_pos)
        val underder=actor.world.getBlockState(under_pos.down())
        if(under.isAir && underder.isAir){
            actionTick=actor.buildAt(under_pos)
        }

        // Other
        else if(actionTick<=0 && target!=null){
            val offset=target.y-actor.pos.y

            // Break under
            if(offset <= -2){
                val under=actor.world.getBlockState(actor.blockPos.down())
                if (!under.isAir && !actor.navigation.isFollowingPath){
                    actor.moveControl.moveTo(actor.blockPos.x+0.5, actor.blockPos.y.toDouble()-1f, actor.blockPos.z+0.5, 1.0)
                    actor.navigation.stop()
                    actionTick=actor.breakAt(actor.blockPos.down())
                }
            }
            else if(offset>=2) {
                // Break block over
                val over=actor.world.getBlockState(actor.blockPos.up().up())
                if (!over.isAir)actionTick=actor.breakAt(actor.blockPos.up().up())
                else{
                    // Put block under and jump
                    actor.setPosition(actor.pos.add(0.0, 1.2, 0.0))
                    actor.moveControl.moveTo(actor.blockPos.x+0.5, actor.blockPos.y.toDouble(), actor.blockPos.z+0.5, 1.0)
                    actor.navigation.stop()
                    actionTick=actor.buildAt(actor.blockPos.down())
                }
            }

            if(actionTick<5)actionTick=5
        }
        else actionTick--
    }
}