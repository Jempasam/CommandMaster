package commandmaster.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import commandmaster.components.CmdMastComponents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.component.DataComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipAppender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class TooltipMixin {
	@Shadow protected abstract <T extends TooltipAppender> void appendTooltip(DataComponentType<T> componentType, Consumer<Text> textConsumer, TooltipContext context);

	@ModifyReturnValue(at=@At("RETURN"), method="getTooltip")
	public List<Text> modifyGetTooltip(List<Text> original, @Local(argsOnly = true) TooltipContext context) {
		var index= new AtomicInteger(1);
		Consumer<Text> appender = text->original.add(index.getAndIncrement(),text);
		appendTooltip(CmdMastComponents.INSTANCE.getMACRO_HOLDER(), appender, context);
		appendTooltip(CmdMastComponents.INSTANCE.getUPGRADER_COMPONENT(), appender, context);
		return original;
	}
}