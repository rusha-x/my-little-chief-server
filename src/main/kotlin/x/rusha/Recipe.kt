package x.rusha

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: Int,
    val name: String,
    val description: String,
    val ingredients: List<Ingredient>
) {
    @Serializable
    data class Ingredient(
        val countInRecipe: Double,
        val product: Product
    )
}