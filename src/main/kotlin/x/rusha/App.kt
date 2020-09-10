package x.rusha

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

fun main() {

    val driver: SqlDriver = JdbcSqliteDriver(url = "jdbc:sqlite:../MyLittleChief.db")
    createSchema(driver)

    val db = LittleChiefDatabase(driver)

    embeddedServer(Netty, port = 9999) {

        install(ContentNegotiation) {
            json()
        }

        routing {
            get("recipe/all") {
                val recipesRows = db.recipesQueries
                    .selectRecipes()
                    .executeAsList()
                val recipes = recipesRows.map(fun(recipesRow): Recipe {
                    val ingredientsRows = db.ingredientsQueries
                        .selectIngredientsByRecipeId(recipe_id = recipesRow._id)
                        .executeAsList()
                    val ingredients = ingredientsRows.map(fun(ingredientRow): Recipe.Ingredient {
                        val productRow = db.productsQueries
                            .selectProduct(product_id = ingredientRow.product_id)
                            .executeAsOne()
                        return Recipe.Ingredient(
                            countInRecipe = ingredientRow.count_in_recipe,
                            product = Product(
                                id = productRow._id.toInt(),
                                name = productRow.name,
                                price = productRow.price,
                                unit = productRow.unit
                            )
                        )
                    })
                    return Recipe(
                        id = recipesRow._id.toInt(),
                        name = recipesRow.name,
                        description = recipesRow.description,
                        ingredients = ingredients
                    )
                })
                call.respond(recipes)
            }
            put("recipe") {
                val recipe = call.receive<CreateOrEditRecipe>()
                db.transaction {
                    val recipeId: Long
                    if (recipe.id != null) {
                        db.recipesQueries.updateRecipe(
                            name = recipe.name,
                            description = recipe.description,
                            _id = recipe.id.toLong()
                        )
                        recipeId = recipe.id.toLong()
                    } else {
                        db.recipesQueries.insertRecipes(
                            name = recipe.name,
                            description = recipe.description
                        )
                        recipeId = db.recipesQueries.lastRecipeId().executeAsOne()
                    }
                    db.ingredientsQueries.deleteIngridientsByRecipeId(recipeId)
                    recipe.ingredients.forEach(fun(ingredient: CreateOrEditRecipe.Ingredient) {
                        val productId: Long
                        if (ingredient.product.id != null) {
                            db.productsQueries.updateProduct(
                                name = ingredient.product.formattedName(),
                                price = ingredient.product.price,
                                unit = ingredient.product.unit,
                                _id = ingredient.product.id.toLong()
                            )
                            productId = ingredient.product.id.toLong()
                        } else {
                            val productIdByName = db.productsQueries.selectProductIdByName(
                                name = ingredient.product.formattedName()
                            ).executeAsOneOrNull()
                            if (productIdByName != null) {
                                db.productsQueries.updateProduct(
                                    name = ingredient.product.formattedName(),
                                    price = ingredient.product.price,
                                    unit = ingredient.product.unit,
                                    _id = productIdByName
                                )
                                productId = productIdByName
                            } else {
                                db.productsQueries.insertProduct(
                                    name = ingredient.product.formattedName(),
                                    price = ingredient.product.price,
                                    unit = ingredient.product.unit
                                )
                                productId = db.productsQueries.lastProductId().executeAsOne()
                            }
                        }
                        db.ingredientsQueries.insertIngredients(
                            count_in_recipe = ingredient.countInRecipe,
                            recipe_id = recipeId,
                            product_id = productId
                        )
                    })
                }
                call.respond("")
            }
        }
    }.start(wait = true)
}

private fun createSchema(driver: SqlDriver) {
    val schemaVersionFile = File("./schema_version.txt")
    val schemaVersion = if (schemaVersionFile.exists()) {
        val versionString = schemaVersionFile.readText()
        versionString.toInt()
    } else {
        0
    }
    if (schemaVersion < LittleChiefDatabase.Schema.version) {
        LittleChiefDatabase.Schema.create(driver)
        driver.execute(identifier = null, sql = "PRAGMA foreign_keys=ON;", parameters = 0)
        schemaVersionFile.writeText(LittleChiefDatabase.Schema.version.toString())
    }
}