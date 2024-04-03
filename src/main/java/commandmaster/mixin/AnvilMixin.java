package commandmaster.mixin;

import commandmaster.components.CmdMastComponents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public class AnvilMixin extends ForgingScreenHandler {

    @Shadow @Final private Property levelCost;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    public void updateResult(CallbackInfo ci){
        do{
            // Upgraded item
            var upgraded=this.input.getStack(0);
            if(upgraded.isEmpty())break;

            // Upgrader
            var upgrader=this.input.getStack(1).get(CmdMastComponents.INSTANCE.getUPGRADER_COMPONENT());
            if(upgrader==null)break;

            // Can upgrade?
            if(upgrader.getTarget().isPresent() && !upgraded.isIn(upgrader.getTarget().get()))break;

            // Result
            var result=upgraded.copy();
            result.applyChanges(upgrader.getComponents());
            this.output.setStack(0,result);
            this.levelCost.set(upgrader.getLevelCost());
            ci.cancel();
        }while(false);
    }

    public AnvilMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Shadow
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {return false;}

    @Shadow
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) { }

    @Shadow
    protected boolean canUse(BlockState state) {return false;}

    @Shadow
    public void updateResult() { }

    @Shadow
    protected ForgingSlotsManager getForgingSlotsManager() {return null;}
}
