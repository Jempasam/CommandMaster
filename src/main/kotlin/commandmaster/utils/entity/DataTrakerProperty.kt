package commandmaster.utils.entity

import net.minecraft.entity.Entity
import net.minecraft.entity.data.TrackedData

class DataTrakerProperty<T>(val data: TrackedData<T>) {

    operator fun getValue(entity: Entity, property: Any): T {
        return entity.dataTracker.get(data)
    }

    operator fun setValue(entity: Entity, property: Any, value: T) {
        entity.dataTracker.set(data, value)
    }

}

fun<T> dataTracked(data: TrackedData<T>) = DataTrakerProperty(data)