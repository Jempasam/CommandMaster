package commandmaster.utils.builders

import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtByteArray
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtIntArray
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtLong
import net.minecraft.nbt.NbtString


// Litterals
fun nbt(value: Byte) = NbtByte.of(value)
val Byte.nbt get() = NbtByte.of(this)
fun nbt(value: Int) = NbtInt.of(value)
val Int.nbt get() = NbtInt.of(this)
fun nbt(value: Long) = NbtLong.of(value)
val Long.nbt get() = NbtLong.of(this)

fun nbt(value: String) = NbtString.of(value)
val String.nbt get() = NbtString.of(this)

fun nbt(value: Float) = NbtFloat.of(value)
val Float.nbt get() = NbtFloat.of(this)
fun nbt(value: Double) = NbtDouble.of(value)
val Double.nbt get() = NbtDouble.of(this)

// Compound
fun nbt(vararg pairs: Pair<String, NbtElement>) = NbtCompound().apply{ for((key, value) in pairs)this.put(key, value) }

// List
fun<T: NbtElement> nbt(vararg values: T) = NbtList().apply{ for(value in values)this.add(value) }

fun nbt(vararg values: Byte) = NbtList().apply{ for(value in values)this.add(NbtByte.of(value)) }
fun nbt(vararg values: Int) = NbtList().apply{ for(value in values)this.add(NbtInt.of(value)) }
fun nbt(vararg values: Long) = NbtList().apply{ for(value in values)this.add(NbtLong.of(value)) }
fun nbt(vararg values: String) = NbtList().apply{ for(value in values)this.add(NbtString.of(value)) }
fun nbt(vararg values: Float) = NbtList().apply{ for(value in values)this.add(NbtFloat.of(value)) }
fun nbt(vararg values: Double) = NbtList().apply{ for(value in values)this.add(NbtDouble.of(value)) }
