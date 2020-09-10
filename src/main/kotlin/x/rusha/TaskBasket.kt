package x.rusha

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.serialization.Serializable

@Serializable
data class Basket(
    val items: List<Item>
) {
    @Serializable
    data class Item(
        val count: Double,
        val product: CreateOrEditProductByName
    )
}

fun main() {
    val driver: SqlDriver = JdbcSqliteDriver(url = "jdbc:sqlite:/Users/adev/code/my-little-chief/MyLittleChief.db")
    LittleChiefDatabase.Schema.create(driver)
    driver.execute(identifier = null, sql = "PRAGMA foreign_keys=ON;", parameters = 0)

    val db = LittleChiefDatabase(driver)
    val basket = Basket(
        items = listOf(
            Basket.Item(
                count = 2.0,
                product = CreateOrEditProductByName(
                    name = "cucumber",
                    price = 50.0,
                    unit = "u."
                )
            ),
            Basket.Item(
                count = 3.0,
                product = CreateOrEditProductByName(
                    name = "milk",
                    price = 30.0,
                    unit = "lt."
                )
            ),
            Basket.Item(
                count = 5.0,
                product = CreateOrEditProductByName(
                    name = "tvorog",
                    price = 15.0,
                    unit = "kg."
                )
            )
        )
    )

    db.basketQueries.insertBaskets()
    val basketsId = db.basketQueries.lastBasketId().executeAsOne()
    basket.items.forEach(fun(item: Basket.Item) {
        db.productsQueries.insertProduct(
            name = item.product.formattedName(),
            price = item.product.price,
            unit = item.product.unit
        )
        val productId = db.productsQueries.lastProductId().executeAsOne()
        db.itemsQueries.insertItem(
            count = item.count,
            basket_id = basketsId,
            product_id = productId
        )
    })
}