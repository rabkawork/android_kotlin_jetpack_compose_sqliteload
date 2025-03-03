/***
 * @author: Ahadian Akbar
 * @date : 28 Feb 2024
 */

package com.example.kotlinsqlite.dao

class ItemRepository(private val ItemDao: ItemDao) {
    suspend fun getAllItems() = ItemDao.getAllItems()
    suspend fun insertItem(item: Item) = ItemDao.insert(item)
    suspend fun updateItem(item: Item) = ItemDao.update(item)
    suspend fun deleteItem(item: Item) = ItemDao.delete(item)
}
