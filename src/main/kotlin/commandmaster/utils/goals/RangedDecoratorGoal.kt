package commandmaster.utils.goals

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.HostileEntity
import java.util.*

class RangedDecoratorGoal(goal: Goal, val actor: HostileEntity, distance: ClosedRange<Float>): DecoratorGoal(goal) {
    val distance=distance.start*distance.start..distance.endInclusive*distance.endInclusive
    override fun canStart() = goal.canStart() && actor.target!=null && distance.contains(actor.squaredDistanceTo(actor.target))
    override fun shouldContinue() = goal.shouldContinue() && actor.target!=null && distance.contains(actor.squaredDistanceTo(actor.target))
}

fun Goal.ranged(actor: HostileEntity, distance: ClosedRange<Float>) = RangedDecoratorGoal(this, actor, distance)