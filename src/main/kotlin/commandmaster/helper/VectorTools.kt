package slabmagic.helper

import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper.atan2
import net.minecraft.util.math.MathHelper.wrapDegrees
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/* TO ANGLE */
fun Direction.asAngle() = Vec2f(offsetY*-90f,asRotation())

fun Vec3d.asAngle(): Vec2f{
    val g = sqrt(x * x + z * z)
    return Vec2f(
        wrapDegrees((-(atan2(y, g) * 57.2957763671875)).toFloat()),
        wrapDegrees((atan2(z, x) * 57.2957763671875).toFloat() - 90.0f)
    )
}

/* TO VECTOR */
fun Direction.asVector() = Vec3d(offsetX.toDouble(),offsetY.toDouble(),offsetZ.toDouble())

fun Vec2f.asVector(): Vec3d{
    val f = cos(-y * 0.017453292 - 3.1415927)
    val f1 = sin(-y * 0.017453292 - 3.1415927)
    val f2 = cos(-x * 0.017453292)
    val f3 =sin(-x * 0.017453292)
    return Vec3d(f1 * f2, f3, f * f2)
}

/* OPERATORS */
operator fun Vec3d.plus(other: Vec3d) = add(other)
operator fun Vec3d.minus(other: Vec3d) = subtract(other)
operator fun Vec3d.times(other: Vec3d) = multiply(other)
