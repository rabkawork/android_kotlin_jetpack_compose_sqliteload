/***
 * @author: Ahadian Akbar
 * @date : 28 Feb 2024
 */

package com.example.kotlinsqlite.dao

import com.example.kotlinsqlite.dao.ItemDao
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    val allItems: Flow<List<Item>> = itemDao.getAllItems()

    suspend fun addItem(name: String, quantity: Int) {
        val newItem = Item(name = name, quantity = quantity)
        itemDao.insertItem(newItem)
    }

    suspend fun deleteItem(item: Item) {
        itemDao.deleteItem(item)
    }

    suspend fun updateItem(item: Item) {
        itemDao.updateItem(item)
    }
}
