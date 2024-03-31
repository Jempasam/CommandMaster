package commandmaster.block

import commandmaster.blockentity.CmdMastBlockEntities
import commandmaster.blockentity.FullComponentBlockEntity
import commandmaster.blockentity.getComponentBlockEntity
import commandmaster.components.CmdMastComponents
import commandmaster.macro.MacroCommand
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.component.Component
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties.*
import net.minecraft.text.Text
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.WorldView
import slabmagic.helper.asAngle

class MachineBlock(settings: Settings) : BlockWithEntity(settings) {
    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH).with(TRIGGERED,false)
    }

    companion object{
        val CODEC= createCodec(::MachineBlock)
    }

    override fun getCodec() = CODEC

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos, notify: Boolean) {
        val power=state.get(TRIGGERED)
        val hasSignal=world.isReceivingRedstonePower(pos)
        if(hasSignal){
            if(!power){
                world.setBlockState(pos, state.with(TRIGGERED, true))
                world.scheduleBlockTick(pos, this, 1)
            }
        }
        else if(power)world.setBlockState(pos, state.with(TRIGGERED, false))
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random?) {
        val bentity = world.getComponentBlockEntity(pos)
        val macro = bentity?.get(CmdMastComponents.MACRO_HOLDER)
        if (macro != null) {
            val command=macro.build(listOf())
            if(command!=null) {
                val source= ServerCommandSource(
                    CommandOutput.DUMMY,
                    Vec3d.ofCenter(pos), state.get(FACING).asAngle(),
                    world, 2,
                    "Machine", bentity.get(DataComponentTypes.CUSTOM_NAME) ?: Text.of("Machine"),
                    world.server, null
                )
                world.server.let { MacroCommand.executeMultiline(it,source,command) }
            }
        }
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return CmdMastBlockEntities.MACHINE_BLOCK.instantiate(pos, state)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(FACING, TRIGGERED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultState.with(CommandBlock.FACING, ctx.playerLookDirection.opposite)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(
            CommandBlock.FACING,
            rotation.rotate(state.get(CommandBlock.FACING) as Direction)
        )
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state.get(CommandBlock.FACING) as Direction))
    }

    override fun getPickStack(world: WorldView, pos: BlockPos, state: BlockState): ItemStack {
        val ret=super.getPickStack(world, pos, state)
        fun<T> put(comp: Component<T>)= ret.set(comp.type,comp.value)
        world.getComponentBlockEntity(pos)?.createComponentMap()?.let { ret.applyComponentsFrom(it) }
        return ret
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}