package commandmaster.item

import commandmaster.CommandMaster
import commandmaster.components.CmdMastComponents.MACRO_HOLDER
import commandmaster.components.CmdMastComponents.MACRO_COMPLETION
import commandmaster.macro.MacroCommand
import commandmaster.macro.MacroCompletion
import commandmaster.macro.MacroParamType
import commandmaster.macro.MacroUtils
import net.minecraft.block.pattern.CachedBlockPosition
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.screen.slot.Slot
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.world.World

class CommandWandItem(settings: Settings) : Item(settings) {

    fun tryToCast(player: ServerPlayerEntity, server:MinecraftServer?, hand: Hand, stack: ItemStack) {
        val macro=stack.get(MACRO_HOLDER)
        val state=stack.get(MACRO_COMPLETION) ?: MacroCompletion()
        if(macro==null)return

        val command=macro.build(state).getOrElse{ return }
        stack.damage(1,player,if(hand==Hand.MAIN_HAND) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND)
        stack.remove(MACRO_COMPLETION)
        server?.let { MacroCommand.executeMultiline(it,player.commandSource.withMaxLevel(3).withSilent(),command) }
    }

    fun<T> getAndCast(player:PlayerEntity?, world: World, stack: ItemStack, hand: Hand, value: T, converter: MacroParamType.(T)->String?): ActionResult {
        if(player !is ServerPlayerEntity)return ActionResult.SUCCESS
        if(world !is ServerWorld)return ActionResult.SUCCESS

        val macro=stack.get(MACRO_HOLDER)
        if(macro==null)return ActionResult.FAIL

        val completion=stack.get(MACRO_COMPLETION) ?: MacroCompletion()
        if(completion.size<macro.parameters.size){
            val arg=macro.parameters[completion.size]
            try{
                stack.set(MACRO_COMPLETION, completion.modify{add(value,converter, arg)})
            }catch(e: MacroCompletion.InvalidTarget){
                player.sendMessage(CommandMaster.translatable("message","invalid_target").formatted(Formatting.RED))
                return ActionResult.FAIL
            }
        }
        tryToCast(player,world.server, hand, stack)
        return ActionResult.SUCCESS
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        return getAndCast(context.player, context.world, context.stack, context.hand, CachedBlockPosition(context.world,context.blockPos,true), MacroParamType::of)
    }

    override fun useOnEntity(stack: ItemStack, player: PlayerEntity, entity: LivingEntity, hand: Hand): ActionResult {
        return getAndCast(player, player.world, stack, hand, entity, MacroParamType::of)
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack=player.getStackInHand(hand)
        return getAndCast(player, player.world, stack, hand, player, MacroParamType::of)
            .let { TypedActionResult(it,stack) }
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