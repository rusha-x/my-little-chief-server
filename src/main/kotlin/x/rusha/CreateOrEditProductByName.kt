package x.rusha

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrEditProductByName(
    val id: Int? = null,
    private val name: String,
    val price: Double,
    val unit: String
) {
    fun formattedName(): String = name.toLowerCase().capitalize().replace("  ", " ")
}