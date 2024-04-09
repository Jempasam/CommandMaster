package commandmaster.mixin;

import commandmaster.components.CmdMastComponents;
import commandmaster.utils.components.ComponentHelperKt;
import net.minecraft.block.BlockState;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
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

import java.util.Optional;

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

            // Get Change list
            ComponentChanges components;
            if(upgrader.getMerge()){
                var merged=upgrader.getComponents();
                var target=upgraded.getComponentChanges();
                var builder=ComponentChanges.builder();
                for(var m : merged.entrySet()){
                    merge(builder,target,m.getKey(),merged);
                }
                components=builder.build();
            }
            else{
                components=upgrader.getComponents();
            }

            // Result
            var result=upgraded.copy();
            result.applyChanges(components);
            this.output.setStack(0,result);
            this.levelCost.set(upgrader.getLevelCost());
            ci.cancel();
        }while(false);
    }

    <T> void merge(ComponentChanges.Builder result, ComponentChanges target, DataComponentType<T> type, ComponentChanges upgrader){
        var uc=upgrader.get(type);
        var tc=target.get(type);
        if(uc==null);
        if(tc==null)tc= Optional.empty();
        var merged=ComponentHelperKt.mergeFrom((Optional<T>)tc,type,(Optional<T>)uc);
        if(merged.isEmpty())result.remove(type);
        else result.add(type,merged.get());
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
