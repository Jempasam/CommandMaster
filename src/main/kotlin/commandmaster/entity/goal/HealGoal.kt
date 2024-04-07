package commandmaster.entity.goal

import commandmaster.entity.trait.AutohealMob
import commandmaster.entity.trait.BuilderMob
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.util.math.BlockPos
import kotlin.math.abs

class HealGoal<T>(val actor:T): Goal() where T: HostileEntity, T: AutohealMob {

    private var actionTick=0

    override fun canStart() = (actor.health < actor.maxHealth/10 || (actor.health<2 && actor.health<actor.maxHealth)) && actor.age%4==0

    override fun start() {
        super.start()
        actionTick=20
    }

    override fun shouldContinue() = actionTick>0

    override fun tick() {
        super.tick()
        actionTick--
        if(actionTick==1)actor.heal()
    }
}