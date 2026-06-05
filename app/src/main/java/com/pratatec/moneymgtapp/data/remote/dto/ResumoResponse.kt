package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ResumoResponse(
    @Serializable(with = FlexibleDoubleSerializer::class)
    val saldo_carteira: Double,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val saldo_disponivel_mes: Double,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val total_gasto_mes: Double,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val limite_hoje: Double,
)

object FlexibleDoubleSerializer : kotlinx.serialization.KSerializer<Double> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
        "FlexibleDouble",
        kotlinx.serialization.descriptors.PrimitiveKind.DOUBLE,
    )

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Double) {
        encoder.encodeDouble(value)
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Double {
        val json = decoder as? kotlinx.serialization.json.JsonDecoder
            ?: return decoder.decodeDouble()
        val element = json.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.isString -> element.content.toDouble()
            element is JsonPrimitive -> element.double
            else -> 0.0
        }
    }
}
