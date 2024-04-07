package commandmaster.entity.goal

import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.item.ItemStack
import java.util.*
import kotlin.reflect.KMutableProperty1

class RangedItemAttackGoal<T>(
    private val container: KMutableProperty1<T,ItemStack>,
    private val actor: T,
    private val speed: Float,
    private val attackInterval: Int,
    range: Double
): Goal() where T:HostileEntity, T:RangedAttackMob
{
    private var squaredRange = range*range
    private var targetSeeingTicker = 0
    private var strafeSide = speed/2
    private var strafeForward = speed/2
    private var combatTicks = 0
    private var attackTicks = 0

    init {
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK))
    }

    override fun canStart(): Boolean{
        actor.target ?: return false
        if(container.get(actor).isEmpty)return false
        return true
    }

    override fun shouldContinue() = targetSeeingTicker>0 && actor.target!=null && !actor.mainHandStack.isEmpty

    override fun start() {
        super.start()
        if(actor.visibilityCache.canSee(actor.target))targetSeeingTicker=5
        actor.equip(container)
    }

    override fun stop() {
        super.stop()
        actor.unequip(container)
    }

    override fun shouldRunEveryTick() = true

    override fun tick() {
        val target = actor.target
        if (target != null) {
            val d = actor.squaredDistanceTo(target.x, target.y, target.z)

            // Check visibility
            if(actor.visibilityCache.canSee(target) && targetSeeingTicker<200)targetSeeingTicker++
            else if(targetSeeingTicker>0)targetSeeingTicker--
            val doSee= targetSeeingTicker >= 20

            // Make a new attack plan each 20 ticks
            if (this.combatTicks >= 20) {
                // Get straffing random direction
                if (actor.random.nextFloat().toDouble() < 0.3) strafeSide = -strafeSide
                if (actor.random.nextFloat().toDouble() < 0.3) strafeForward = -strafeForward

                // If player is too far, walk toward him
                if(doSee && d > squaredRange*1.2){
                    actor.navigation.startMovingTo(target, speed.toDouble())
                }
                else{
                    actor.navigation.stop()
                }

                // Quick adjustment movements
                if(d < squaredRange*0.8) { // Flees if too close
                    actor.moveControl.strafeTo(-speed+strafeForward, strafeSide)
                }
                else{
                    actor.moveControl.strafeTo(strafeForward,strafeSide)
                }

                this.combatTicks = 0
            }
            else this.combatTicks++

            // Look at target
            if(doSee){
                actor.lookControl.lookAt(target)
            }

            // Attack
            if(attackTicks>=attackInterval){
                if(doSee && d < squaredRange*1.5){
                    actor.shootAt(actor.target, 1.0f)
                }
                attackTicks=0
            }
            else attackTicks++
        }
    }
}