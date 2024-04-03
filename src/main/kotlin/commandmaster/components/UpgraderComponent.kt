package commandmaster.components

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import commandmaster.codec.getWith
import commandmaster.codec.invoke
import commandmaster.codec.of
import net.minecraft.client.item.TooltipContext
import net.minecraft.component.ComponentChanges
import net.minecraft.item.Item
import net.minecraft.item.TooltipAppender
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.awt.Color
import java.util.Optional
import java.util.function.Consumer

data class UpgraderComponent(val target: Optional<TagKey<Item>>, val components: ComponentChanges, val levelCost: Int=1, val showInTooltip: Boolean=true): TooltipAppender {

    override fun appendTooltip(textConsumer: Consumer<Text>, context: TooltipContext) {
        if(!showInTooltip)return
        textConsumer.accept(Text.literal("Content:").styled { it.withColor(Formatting.GRAY).withItalic(true) })
        val appender = Consumer<Text>{text -> textConsumer.accept(Text.literal(" + ").styled { it.withColor(Formatting.GRAY).withItalic(true) }.append(text))}
        for((key, value) in components.entrySet()){
            if(value.isPresent) {
                val getted=value.get()
                if(getted is TooltipAppender)getted.appendTooltip(appender,context)
                else if(context.isAdvanced)appender.accept(Text.of("$key"))
            }
            else if(context.isAdvanced)textConsumer.accept(Text.literal(" - $key").styled { it.withColor(Formatting.GRAY).withItalic(true) })
        }
    }

    companion object{
        val CODEC: Codec<UpgraderComponent> = RecordCodecBuilder.create {
            it.group(
                "target" of TagKey.codec(RegistryKeys.ITEM)() getWith UpgraderComponent::target,
                "components" of ComponentChanges.CODEC getWith UpgraderComponent::components,
                "levelCost" of Codec.INT(1) getWith UpgraderComponent::levelCost,
                "showInTooltip" of Codec.BOOL(true) getWith UpgraderComponent::showInTooltip
            ).apply(it, ::UpgraderComponent)
        }

        val EMPTY=UpgraderComponent(Optional.empty(), ComponentChanges.EMPTY, 1, false)
    }
}