package commandmaster.item

import commandmaster.components.CmdMastComponents
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ChargedProjectilesComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.CrossbowItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class HandCatapultItem(settings: Settings) : Item(settings){

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        val chargedProjectilesComponent = itemStack.get(DataComponentTypes.CHARGED_PROJECTILES)
        if (chargedProjectilesComponent != null && !chargedProjectilesComponent.isEmpty) {
            return TypedActionResult.consume(itemStack)
        }
        val projectile = getItem(user)
        if(projectile!=null){
            return TypedActionResult.consume(itemStack)
        }
        return TypedActionResult.fail(itemStack)
    }

    override fun getMaxUseTime(stack: ItemStack) = 30

    override fun isUsedOnRelease(stack: ItemStack) = stack.isOf(this)

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        if(remainingUseTicks<=0){
            val chargedProjectilesComponent = stack.get(DataComponentTypes.CHARGED_PROJECTILES)
            if (chargedProjectilesComponent != null && !chargedProjectilesComponent.isEmpty) {
                user.kill()
            }
            if(user is ServerPlayerEntity){
                val projectile = getItem(user)
                if(projectile!=null){
                    stack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(stack))
                }
            }
        }
    }

    fun getPullProgress(useTicks: Int, stack: ItemStack): Float{
        val comp=stack.get(DataComponentTypes.CHARGED_PROJECTILES)
        if(comp!=null && !comp.isEmpty)return 1f;
        return 1f - (30-useTicks)/30f
    }

    companion object{
        fun getItem(player: PlayerEntity): ItemStack?{
            for (i in 0 until player.inventory.size()) {
                val found = player.inventory.getStack(i)
                val shootable = found.contains(CmdMastComponents.IS_SHOOTABLE)
                if (!shootable) continue
                return found
            }
            return null
        }
    }
}