package commandmaster.entity

import commandmaster.CommandMaster
import commandmaster.entity.CmdMastEntities.attributes
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object CmdMastEntities {

    val MACRO_CREEPER= register("macro_creeper", ::MacroCreeperEntity, SpawnGroup.MONSTER) { dimensions(0.6f,1.7f); maxTrackingRange(7) }
        .attributes(EntityType.CREEPER)

    val SMART_ZOMBIE= register("smart_zombie", ::SmartZombieEntity, SpawnGroup.MONSTER) { dimensions(0.6f,1.95f); maxTrackingRange(7) }
        .attributes(EntityType.ZOMBIE)

    val ITEM_PROJECTILE= register("item_projectile",::UseItemProjectileEntity, SpawnGroup.MISC) { dimensions(0.25f,0.25f); maxTrackingRange(4) }

    inline fun<T: Entity> register(id: String, factory: EntityType.EntityFactory<T>, group: SpawnGroup, builder: EntityType.Builder<T>.()->Unit): EntityType<T> {
        val build=EntityType.Builder.create(factory, group)
        build.builder()
        val identifier=CommandMaster/id
        val type=build.build(identifier.toString())
        Registry.register(Registries.ENTITY_TYPE, identifier, type)
        return type
    }

    inline fun<T: LivingEntity> EntityType<T>.attributes(attributes: DefaultAttributeContainer.Builder.()->Unit): EntityType<T> {
        val builder=DefaultAttributeContainer.builder()
        builder.attributes()
        FabricDefaultAttributeRegistry.register(this,builder)
        return this
    }

    fun<T: LivingEntity> EntityType<T>.attributes(copied: EntityType<out LivingEntity>): EntityType<T> {
        FabricDefaultAttributeRegistry.register(this,DefaultAttributeRegistry.get(copied))
        return this
    }

}