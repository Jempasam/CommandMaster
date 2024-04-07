package commandmaster.utils.goals

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.HostileEntity
import java.util.*

open class DecoratorGoal(protected val goal: Goal): Goal() {
    override fun canStart() = goal.canStart()
    override fun shouldContinue() = goal.shouldContinue()
    override fun stop(){
        println("Decorator Stop")
        goal.stop()
    }
    override fun start() = goal.start()
    override fun tick() = goal.tick()
    override fun canStop() = goal.canStop()
    override fun shouldRunEveryTick() = goal.shouldRunEveryTick()
    override fun getControls() = goal.controls
    override fun setControls(controls: EnumSet<Control>) = goal.setControls(controls)
}