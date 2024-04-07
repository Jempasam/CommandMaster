package commandmaster.entity.trait

import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos

interface BuilderMob {
    fun buildAt(pos: BlockPos): Int
    fun breakAt(pos: BlockPos): Int

}