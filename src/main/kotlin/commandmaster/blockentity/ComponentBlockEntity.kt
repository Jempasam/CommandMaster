package commandmaster.blockentity

import commandmaster.block.CmdMastBlocks
import commandmaster.components.CmdMastComponents
import commandmaster.macro.MacroCommand
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.component.Component
import net.minecraft.component.ComponentMap
import net.minecraft.component.DataComponentType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

open class ComponentBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState): BlockEntity(type, pos, state){

    private fun encodeAll(compound: NbtCompound){
        fun<T> encode(compound: NbtCompound, component: Component<T>){
            component.type.codec?.encodeStart(NbtOps.INSTANCE,component.value)
                ?.result()
                ?.ifPresent { nbt ->
                    Registries.DATA_COMPONENT_TYPE.getKey(component.type).ifPresent { key ->
                        compound.put(key.value.toString(), nbt)
                    }
                }
        }
        for(comp in createComponentMap()){
            encode(compound,comp)
        }
    }

    private fun decodeAll(compound: NbtCompound){
        val builder=ComponentMap.builder()
        for(key in compound.keys){
            val nbt= compound.get(key) ?: continue
            val id= Identifier.tryParse(key) ?: continue
            val type=Registries.DATA_COMPONENT_TYPE.get(id) ?: continue
            fun<T> add(type: DataComponentType<T>){
                type.codec?.parse(NbtOps.INSTANCE,nbt)?.result()?.ifPresent {
                    builder.add(type,it)
                }
            }
            add(type)
        }
        readComponents(builder.build())
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registryLookup)
        val componentsNbt=NbtCompound()
        encodeAll(componentsNbt)
        nbt.put("Components",componentsNbt)
    }

    override fun readNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.readNbt(nbt, registryLookup)
        val componentsNbt=nbt?.get("Components") as? NbtCompound ?: return
        decodeAll(componentsNbt)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound {
        return createNbt(registryLookup)
    }
}