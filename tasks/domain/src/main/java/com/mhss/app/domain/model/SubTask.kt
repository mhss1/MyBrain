package com.mhss.app.domain.model

import com.benasher44.uuid.Uuid
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SubTask(
    @SerialName("title")
    /*
    using JsonNames for this and other properties to fix problems with proguard obfuscation
    from previous app versions that didn't take obfuscation into account
    */
    @JsonNames("title", "a")
    val title: String = "",
    @SerialName("isCompleted")
    @JsonNames("isCompleted", "b")
    val isCompleted: Boolean = false,
    @SerialName("id")
    @JsonNames("id", "c")
    @Serializable(with = UUIDSerializer::class)
    val id: Uuid = Uuid.randomUUID()
)

object UUIDSerializer : KSerializer<Uuid> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Uuid {
        return Uuid.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Uuid) {
        encoder.encodeString(value.toString())
    }
}
