package commandmaster.codec

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import java.util.function.Function

/* Field Type */
// Creation of codec field type

// An optional field type
class OptFieldType<T>(val type: Codec<T>){
    fun field(name: String): MapCodec<Optional<T>>{
        return type.optionalFieldOf(name)
    }
}

class OptDefFieldType<T>(val type: Codec<T>, var def: T, var lifecycle: Lifecycle?=null){
    fun field(name: String): MapCodec<T>{
        if(lifecycle===null) return type.optionalFieldOf(name, def)
        else return type.optionalFieldOf(name, def, lifecycle)
    }
}

operator fun <T> Codec<T>.invoke() = OptFieldType(this)
operator fun <T> Codec<T>.invoke(def: T, lifecycle: Lifecycle?=null) = OptDefFieldType(this, def, lifecycle)

// Array field type
val <T> Codec<T>.LIST get() = this.listOf()

// Map Codec
infix fun <T> String.of(value: Codec<T>) = value.fieldOf(this)
infix fun <T> String.of(value: OptFieldType<T>) = value.field(this)
infix fun <T> String.of(value: OptDefFieldType<T>) = value.field(this)



// Record Codec
infix fun <V,T> MapCodec<T>.getWith (getter: (V)->T) = this.forGetter(getter)