package slabmagic.helper

import net.minecraft.block.AbstractFireBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.item.BlockItem
import net.minecraft.item.DyeItem
import net.minecraft.item.Item
import net.minecraft.item.SpawnEggItem
import net.minecraft.util.DyeColor
import net.minecraft.util.math.ColorHelper.Argb.*
import net.minecraft.util.math.Vec3d
import java.awt.Color

object ColorTools {

    fun vec(color: Int): Vec3d{
        return Vec3d(
            getRed(color)/255.0,
            getGreen(color)/255.0,
            getBlue(color)/255.0
        )
    }

    fun awt(vec: Vec3d): Color{
        return Color(
            vec.x.toFloat(),
            vec.y.toFloat(),
            vec.z.toFloat()
        )
    }

    fun int(vec: Vec3d): Int{
        return getArgb(
            255,
            (vec.x*255).toInt(),
            (vec.y*255).toInt(),
            (vec.z*255).toInt()
        )
    }

    fun ofHash(obj: Any) = obj.hashCode()%0xffffff

    fun of(block: Block): Int {
        return when (block) {
            Blocks.NETHER_PORTAL -> DyeColor.PURPLE.fireworkColor
            else -> block.defaultMapColor.color
        }
    }



    private val entity_colors= mutableMapOf<EntityType<*>,Int>()
    fun of(entity: EntityType<*>): Int {
        return entity_colors.computeIfAbsent(entity){
            SpawnEggItem.forEntity(entity)?.run {
                val c0=getColor(0)
                val c1=getColor(1)
                getArgb(
                    0,
                    (getRed(c0)+getRed(c1))/2,
                    (getGreen(c0)+getGreen(c1))/2,
                    (getBlue(c0)+getBlue(c1))/2
                )
            }
                ?: DyeColor.GRAY.fireworkColor.takeIf { entity.translationKey.contains("iron") }
                ?: DyeColor.ORANGE.fireworkColor.takeIf { entity.translationKey.contains("fire") }
                ?: DyeColor.YELLOW.fireworkColor.takeIf { entity.translationKey.contains("honey") }
                ?: DyeColor.PURPLE.fireworkColor.takeIf { entity.translationKey.contains("ender") }
                ?: DyeColor.PINK.fireworkColor.takeIf { entity.spawnGroup==SpawnGroup.AXOLOTLS }
                ?: DyeColor.BLUE.fireworkColor.takeIf { entity.spawnGroup==SpawnGroup.WATER_CREATURE }
                ?: DyeColor.LIGHT_BLUE.fireworkColor.takeIf { entity.spawnGroup==SpawnGroup.WATER_AMBIENT }
                ?: DyeColor.ORANGE.fireworkColor.takeIf { entity.isFireImmune }
                ?: DyeColor.GREEN.fireworkColor.takeIf { entity.spawnGroup==SpawnGroup.CREATURE }
                ?: DyeColor.BROWN.fireworkColor.takeIf { entity.spawnGroup==SpawnGroup.MISC }
                ?: DyeColor.PURPLE.fireworkColor
        }
    }



    private val item_colors= mutableMapOf<Item,Int>()
    fun of(item: Item): Int {
        fun <T> T.takeIf(item: Item, vararg strings: String): T? {
            return takeIf{ strings.any{item.translationKey.contains(it)} }
        }
        return item_colors.computeIfAbsent(item) {
                (item as? BlockItem)?.block?.defaultMapColor?.color
                    ?: DyeColor.YELLOW.fireworkColor.takeIf (item, "yellow", "gold", "honey")
                    ?: DyeColor.ORANGE.fireworkColor.takeIf (item, "orange", "carrot", "fire", "magma")
                    ?: DyeColor.RED.fireworkColor.takeIf(item, "red", "lava", "blood", "cherry", "hot", "berries")

                    ?: DyeColor.WHITE.fireworkColor.takeIf(item, "white", "light", "snow", "bone")
                    ?: DyeColor.LIGHT_GRAY.fireworkColor.takeIf(item, "light_gray", "iron", "stone", "metal", "silver", "platinum")
                    ?: DyeColor.GRAY.fireworkColor.takeIf(item, "gray", "iron", "stone", "netherite")

                    ?: DyeColor.LIGHT_BLUE.fireworkColor.takeIf(item, "light_blue", "diamond", "ice", "mana", "soul", "fish")
                    ?: DyeColor.CYAN.fireworkColor.takeIf(item, "cyan", "warped", "ocean", "prismarine")
                    ?: DyeColor.BLUE.fireworkColor.takeIf(item, "blue", "water", "aqua", "sea")

                    ?: DyeColor.GREEN.signColor.takeIf(item, "green", "leave", "poison", "jungle", "emerald", "grass", "turtle")
                    ?: DyeColor.LIME.fireworkColor.takeIf(item, "lime", "nature", "grass", "slime", "frog", "experience")

                    ?: DyeColor.BROWN.fireworkColor.takeIf(item, "brown", "wood", "choco", "dirt", "mud", "leather", "flesh")

                    ?: DyeColor.PURPLE.fireworkColor.takeIf(item, "purple", "ender", "witch", "potion", "shulker")
                    ?: DyeColor.PINK.fireworkColor.takeIf(item, "pink", "flesh", "heart", "love", "flower")
                    ?: DyeColor.MAGENTA.fireworkColor.takeIf(item, "magenta", "magic", "soul", "amethyst")

                    ?: DyeColor.BLACK.fireworkColor.takeIf(item, "black", "dark", "void", "obsidian", "coal", "ink", "tar", "oil", "obsidian", "whiter", "coal")

                    ?: DyeColor.BROWN.signColor.takeIf { item.components.contains(DataComponentTypes.FOOD) }

                    ?: (item as? DyeItem)?.color?.signColor
                    ?: DyeColor.ORANGE.fireworkColor.takeIf { item.components.contains(DataComponentTypes.FIRE_RESISTANT) }
                    ?: ofHash(item.translationKey)
        }
    }



    fun of(effect: StatusEffect): Int {
        return effect.color
    }

    val RED=vec(DyeColor.RED.fireworkColor)
    val GREEN=vec(DyeColor.GREEN.fireworkColor)
    val BLUE=vec(DyeColor.BLUE.fireworkColor)
    val YELLOW=vec(DyeColor.YELLOW.fireworkColor)
    val ORANGE=vec(DyeColor.ORANGE.fireworkColor)
    val PURPLE=vec(DyeColor.PURPLE.fireworkColor)
    val PINK=vec(DyeColor.PINK.fireworkColor)
    val LIGHT_BLUE=vec(DyeColor.LIGHT_BLUE.fireworkColor)
    val LIME=vec(DyeColor.LIME.fireworkColor)
    val MAGENTA=vec(DyeColor.MAGENTA.fireworkColor)
    val BROWN=vec(DyeColor.BROWN.fireworkColor)
    val GRAY=vec(DyeColor.GRAY.fireworkColor)
    val LIGHT_GRAY=vec(DyeColor.LIGHT_GRAY.fireworkColor)
    val CYAN=vec(DyeColor.CYAN.fireworkColor)
    val BLACK=vec(DyeColor.BLACK.fireworkColor)
    val WHITE=vec(DyeColor.WHITE.fireworkColor)
}