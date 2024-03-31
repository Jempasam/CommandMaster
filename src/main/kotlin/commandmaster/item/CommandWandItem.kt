package commandmaster.item

import com.mojang.authlib.minecraft.client.MinecraftClient
import commandmaster.components.CmdMastComponents.MACRO_HOLDER
import commandmaster.components.CmdMastComponents.MACRO_STATE
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroParamType
import commandmaster.macro.MacroUtils
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BundleItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.screen.slot.Slot
import net.minecraft.server.MinecraftServer
import net.minecraft.server.function.Macro
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.min

class CommandWandItem(settings: Settings) : Item(settings) {

    fun tryToCast(player: ServerPlayerEntity, server:MinecraftServer?, slot: EquipmentSlot, stack: ItemStack) {
        val macro=stack.get(MACRO_HOLDER)
        val state=stack.get(MACRO_STATE) ?: listOf()
        if(macro==null)return

        val command=macro.build(state)
        if(command==null)return
        stack.damage(1,player,slot)
        stack.remove(MACRO_STATE)
        server?.let { MacroCommand.executeMultiline(it,player.commandSource.withMaxLevel(3).withSilent(),command) }
    }

    inline fun getAndCast(player:PlayerEntity?, world: World, stack: ItemStack, slot: EquipmentSlot, getter: (MacroParamType)->String): ActionResult {
        if(player !is ServerPlayerEntity)return ActionResult.SUCCESS
        if(world !is ServerWorld)return ActionResult.SUCCESS

        val macro=stack.get(MACRO_HOLDER)
        if(macro==null)return ActionResult.FAIL

        val state=stack.get(MACRO_STATE) ?: listOf()
        if(state.size<macro.parameters.size){
            val type=macro.parameters[state.size]
            val value=getter(type)
            val newState=state.toMutableList()
            newState.add(value)
            stack.set(MACRO_STATE,newState)
        }
        tryToCast(player,world.server, slot, stack)
        return ActionResult.SUCCESS
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        return getAndCast(context.player, context.world, context.stack, if(context.hand==Hand.MAIN_HAND) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND)
            { type-> type.selectBlock(context.player as ServerPlayerEntity, context.world as ServerWorld, context.blockPos) }
    }

    override fun useOnEntity(stack: ItemStack, player: PlayerEntity, entity: LivingEntity, hand: Hand): ActionResult {
        return getAndCast(player, player.world, stack, if(hand==Hand.MAIN_HAND) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND)
            { type-> type.selectEntity(player as ServerPlayerEntity, entity) }
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack=player.getStackInHand(hand)
        return getAndCast(player, player.world, stack, if(hand==Hand.MAIN_HAND) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND)
            { type-> type.selectAir(player as ServerPlayerEntity) }.let { TypedActionResult(it,stack) }
    }

    override fun onStackClicked(stack: ItemStack, slot: Slot, clickType: ClickType, player: PlayerEntity): Boolean {
        if(clickType==ClickType.RIGHT){
            if(slot.stack.isEmpty && player.isInCreativeMode){
                slot.setStack(MacroUtils.createSub(stack),stack)
                return true
            }
        }
        return false
    }

    override fun getName(stack: ItemStack) = MacroUtils.getName(stack)

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        MacroUtils.appendTooltip(stack,world,tooltip,context)
    }
}