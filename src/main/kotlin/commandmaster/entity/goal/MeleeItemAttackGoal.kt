package commandmaster.entity.goal

import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.goal.ZombieAttackGoal
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.item.ItemStack
import kotlin.reflect.KMutableProperty1

class MeleeItemAttackGoal<T>(
    private val container: KMutableProperty1<T,ItemStack>,
    val actor: T, speed: Double, pauseWhenMobIdle: Boolean,
): ZombieAttackGoal(actor,speed, pauseWhenMobIdle) where T:ZombieEntity, T:RangedAttackMob
{
    override fun canStart() = super.canStart() && !container.get(this.actor).isEmpty

    override fun shouldContinue() = super.shouldContinue() && !actor.mainHandStack.isEmpty

    override fun start() {
        actor.equip(container)
        super.start()
    }

    override fun stop() {
        actor.unequip(container)
        super.stop()
    }
}