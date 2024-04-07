package commandmaster.entity

import commandmaster.entity.goal.BridgeToTargetGoal
import commandmaster.entity.goal.MeleeItemAttackGoal
import commandmaster.entity.goal.RangedItemAttackGoal
import commandmaster.entity.trait.AutohealMob
import commandmaster.entity.trait.BuilderMob
import commandmaster.utils.entity.dataTracked
import commandmaster.utils.goals.ranged
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import slabmagic.simulator.asPlayer

open class SmartZombieEntity(entityType: EntityType<out SmartZombieEntity>, world: World) : ZombieEntity(entityType, world), RangedAttackMob, BuilderMob, AutohealMob {

    val player=if(!world.isClient) this.asPlayer() else null

    var melee= ItemStack.EMPTY
    var ranged= ItemStack.EMPTY
    var building= ItemStack.EMPTY
    var breaker= ItemStack.EMPTY
    var healer= ItemStack.EMPTY

    var color by dataTracked(COLOR)

    override fun onAttacking(target: Entity) {
        super.onAttacking(target)
        println("OnAttack")
        val stack=getStackInHand(activeHand)
        if(!stack.isEmpty && player!=null && target is LivingEntity){
            stack.postHit(target,player)
            stack.useOnEntity(player, target, activeHand)
            stack.use(world,player,activeHand)
        }
    }
    override fun shootAt(target: LivingEntity, pullProgress: Float) {
        val stack=getStackInHand(activeHand)
        if(!stack.isEmpty && player!=null){
            stack.use(world, player, activeHand)
        }
    }

    override fun buildAt(pos: BlockPos): Int {
        val hand=if(activeHand===Hand.MAIN_HAND)Hand.OFF_HAND else Hand.MAIN_HAND
        val stack=getStackInHand(hand)
        if(player!=null && !stack.isEmpty){
            fun buildFrom(direction: Direction): Boolean{
                val base=pos.add(direction.opposite.vector)
                if(!world.getBlockState(base).isAir){
                    stack.useOnBlock(ItemUsageContext(player, hand, BlockHitResult(Vec3d.ofCenter(base),direction,base,false)))
                    return true
                }
                return false
            }
            buildFrom(Direction.UP) || buildFrom(Direction.NORTH) || buildFrom(Direction.SOUTH) || buildFrom(Direction.EAST) || buildFrom(Direction.WEST) || return 5
            return 10
        }
        return 5
    }

    override fun heal(): Int {
        val stack=getStackInHand(activeHand)
        if(player!=null && !stack.isEmpty){
            val usage=stack.use(world,player,activeHand)
            if(usage.result==ActionResult.CONSUME)stack.onStoppedUsing(world,player,0)
        }
        return 1
    }

    override fun breakAt(pos: BlockPos): Int {
        val hand=if(activeHand===Hand.MAIN_HAND)Hand.OFF_HAND else Hand.MAIN_HAND
        val stack=getStackInHand(hand)
        if(stack.canBreak(CachedBlockPosition(world,pos,false))){

        }
        if(!world.isClient)world.breakBlock(pos,false)
        return 0
    }

    override fun burnsInDaylight() = false

    override fun initCustomGoals() {
        //goalSelector.add(2,HealGoal(this))
        goalSelector.add(1, BridgeToTargetGoal(SmartZombieEntity::building, this))
        goalSelector.add(2, RangedItemAttackGoal(SmartZombieEntity::ranged, this, 1.0f, 20, 10.0).ranged(this, 6f..100f))
        goalSelector.add(4, MeleeItemAttackGoal(SmartZombieEntity::melee, this, 1.5, true))
        goalSelector.add(5, ZombieAttackGoal(this, 1.5, true))
        goalSelector.add(7, WanderAroundFarGoal(this, 1.0))
        targetSelector.add(1, RevengeGoal(this))
        targetSelector.add(2, ActiveTargetGoal(this, PlayerEntity::class.java, true))
        targetSelector.add(2, ActiveTargetGoal(this, HostileEntity::class.java, true))
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        nbt.getCompound("melee") ?.let { melee=ItemStack.fromNbtOrEmpty(registryManager,it) }
        nbt.getCompound("ranged") ?.let { ranged=ItemStack.fromNbtOrEmpty(registryManager,it) }
        nbt.getCompound("building") ?.let { building=ItemStack.fromNbtOrEmpty(registryManager,it) }
        nbt.getCompound("breaker") ?.let { breaker=ItemStack.fromNbtOrEmpty(registryManager,it) }
        nbt.getCompound("healer") ?.let { healer=ItemStack.fromNbtOrEmpty(registryManager,it) }
        nbt.getInt("color") .takeIf{it!=0} ?.let { color = it }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        melee ?.takeIf { !it.isEmpty } ?.let { nbt.put("melee", it.encode(registryManager)) }
        ranged ?.takeIf { !it.isEmpty } ?.let { nbt.put("ranged", it.encode(registryManager)) }
        building ?.takeIf { !it.isEmpty } ?.let { nbt.put("building", it.encode(registryManager)) }
        breaker ?.takeIf { !it.isEmpty } ?.let { nbt.put("breaker", it.encode(registryManager)) }
        healer ?.takeIf { !it.isEmpty } ?.let { nbt.put("healer", it.encode(registryManager)) }
        nbt.putInt("color", color)
    }

    override fun initDataTracker(builder: DataTracker.Builder) {
        super.initDataTracker(builder)
        builder.add(COLOR, DyeColor.WHITE.fireworkColor)
    }

    companion object{
        val COLOR = DataTracker.registerData(SmartZombieEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
    }
}