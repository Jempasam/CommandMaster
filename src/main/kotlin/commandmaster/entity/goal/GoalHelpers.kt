package commandmaster.entity.goal

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.Hand.*
import kotlin.reflect.KMutableProperty1

fun <T: LivingEntity> T.equip(from: KMutableProperty1<T,ItemStack>, otherHand: Boolean=false){
    val hand=if(otherHand) (if(activeHand===MAIN_HAND) OFF_HAND else MAIN_HAND) else activeHand
    if(!getStackInHand(hand).isEmpty)dropStack(getStackInHand(hand))
    setStackInHand(hand,from.get(this))
    from.set(this,ItemStack.EMPTY)
}

fun <T: LivingEntity> T.unequip(to: KMutableProperty1<T,ItemStack>, otherHand: Boolean=false){
    val hand=if(otherHand) (if(activeHand===MAIN_HAND) OFF_HAND else MAIN_HAND) else activeHand
    if(!to.get(this).isEmpty)dropStack(to.get(this))
    to.set(this,getStackInHand(hand))
    setStackInHand(hand,ItemStack.EMPTY)
}