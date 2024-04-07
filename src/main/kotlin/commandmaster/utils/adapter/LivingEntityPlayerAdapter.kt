package slabmagic.simulator

import com.mojang.authlib.GameProfile
import net.fabricmc.fabric.api.entity.FakePlayer
import net.minecraft.entity.*
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BowItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.UseAction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*


inline fun LivingEntity.asPlayer(action: (ServerPlayerEntity)->Unit){
    // Keep old adapter or create a new adapter
    var current_adapter=_adapter
    if(current_adapter==null || current_adapter.world!=this.world){
        current_adapter=LivingEntityPlayerAdapter(this)
    }

    // Do something
    synchronized(current_adapter){
        current_adapter.setEntity(this)
        action(current_adapter)
    }

    _adapter=current_adapter
}

fun LivingEntity.asPlayer(): ServerPlayerEntity = LivingEntityPlayerAdapter(this)

var _adapter: LivingEntityPlayerAdapter?=null

class LivingEntityPlayerAdapter(entity: LivingEntity)
    : FakePlayer(entity.world as ServerWorld, GameProfile(UUID.fromString("0e54f11f-14d7-47a7-b9a9-9c3fe54773d1"), "[Adapter of samdemont06]"))
{

    private var entity: LivingEntity? = null

    init {
        setEntity(entity)
    }

    fun setEntity(entity: LivingEntity){
        this.entity=entity
        super.setPosition(entity.pos.x, entity.pos.y, entity.pos.z)
    }

    override fun isSpectator() = false
    override fun isCreative() = false
    override fun kill() { entity?.kill() }
    override fun addCommandTag(tag: String) = entity?.addCommandTag(tag) ?: false

    // Position
    override fun getBlockPos() = entity?.blockPos ?: BlockPos.ORIGIN
    override fun getPos() = entity?.pos ?: Vec3d.ZERO
    override fun getX() = entity?.x ?: 0.0
    override fun getY() = entity?.y ?: 0.0
    override fun getEyeY() = entity?.eyeY ?: 0.0
    override fun getZ() = entity?.z ?: 0.0
    override fun setPosition(x: Double, y: Double, z: Double){ entity?.setPosition(x,y,z) }
    override fun setPos(x: Double, y: Double, z: Double){ entity?.setPos(x,y,z) }


    // Equipment
    override fun getEquippedStack(slot: EquipmentSlot) = entity?.getEquippedStack(slot) ?: ItemStack.EMPTY
    override fun equipStack(slot: EquipmentSlot, stack: ItemStack) { entity?.equipStack(slot,stack)}
    override fun getArmorItems() = entity?.armorItems ?: List(4){ItemStack.EMPTY}
    override fun getActiveItem() = entity?.activeItem ?: ItemStack.EMPTY
    override fun getActiveHand() = entity?.activeHand ?: Hand.MAIN_HAND

    // Health
    override fun getHealth() = entity?.health ?: 1f
    override fun damage(source: DamageSource, amount: Float) = entity?.damage(source,amount) ?: false

    // Rotation
    override fun getPitch() = entity?.getPitch(0f) ?: 0f
    override fun getYaw() = entity?.getYaw(0f) ?: 0f
    override fun setPitch(pitch: Float){ entity?.pitch=pitch }
    override fun setYaw(yaw: Float){ entity?.yaw=yaw }
    override fun getHeadYaw() = entity?.headYaw ?: 0f
    override fun getBodyYaw() = entity?.bodyYaw ?: 0f
    override fun onAttacking(target: Entity) { entity?.onAttacking(target) }

    // IDs
    override fun getUuid() = entity?.uuid ?: UUID.randomUUID()
    override fun getUuidAsString() = entity?.uuidAsString ?: UUID.randomUUID().toString()
    override fun getId() = entity?.id ?: 0
}