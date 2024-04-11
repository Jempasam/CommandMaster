package commandmaster.components

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import commandmaster.codec.getWith
import commandmaster.codec.invoke
import commandmaster.codec.of
import net.minecraft.client.item.TooltipType
import net.minecraft.component.ComponentChanges
import net.minecraft.item.Item
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.TooltipAppender
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.Optional
import java.util.function.Consumer

data class UpgraderComponent(val target: Optional<TagKey<Item>>, val components: ComponentChanges, val levelCost: Int=1, val showInTooltip: Boolean=true, val merge: Boolean=false): TooltipAppender {

    override fun appendTooltip(context: TooltipContext, consumer: Consumer<Text>, type: TooltipType) {
        if(!showInTooltip)return
        consumer.accept(Text.literal("Content:").styled { it.withColor(Formatting.GRAY).withItalic(true) })
        val appender = Consumer<Text>{text -> consumer.accept(Text.literal(" + ").styled { it.withColor(Formatting.GRAY).withItalic(true) }.append(text))}
        for((key, value) in components.entrySet()){
            if(value.isPresent) {
                val getted=value.get()
                if(getted is TooltipAppender)getted.appendTooltip(context,consumer,type)
                else if(type.isAdvanced)appender.accept(Text.of("$key"))
            }
            else if(type.isAdvanced)consumer.accept(Text.literal(" - $key").styled { it.withColor(Formatting.GRAY).withItalic(true) })
        }
    }

    companion object{
        val CODEC: Codec<UpgraderComponent> = RecordCodecBuilder.create {
            it.group(
                "target" of TagKey.codec(RegistryKeys.ITEM)() getWith UpgraderComponent::target,
                "components" of ComponentChanges.CODEC getWith UpgraderComponent::components,
                "levelCost" of Codec.INT(1) getWith UpgraderComponent::levelCost,
                "showInTooltip" of Codec.BOOL(true) getWith UpgraderComponent::showInTooltip,
                "merge" of Codec.BOOL(false) getWith UpgraderComponent::merge
            ).apply(it, ::UpgraderComponent)
        }

        val EMPTY=UpgraderComponent(Optional.empty(), ComponentChanges.EMPTY, 1, false)
    }
}