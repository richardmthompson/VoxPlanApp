package com.voxplanapp.data


import androidx.compose.ui.input.pointer.PointerId
import androidx.room.*
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM TodoItem")
    fun getAllTodos(): Flow<List<TodoItem>>

    // get specific todo
    @Query("SELECT * from TodoItem WHERE id = :id")
    fun getItem(id: Int): Flow<TodoItem>

    @Query("SELECT * FROM TodoItem WHERE parentID = null")
    fun getRootTodos(): List<TodoItem>

    @Query("SELECT * FROM TodoItem WHERE id IN (:ids)")
    fun getItemsByIds(ids: List<Int>): Flow<List<TodoItem>>

    @Query("SELECT * FROM TodoItem WHERE parentID = :parentId")
    fun getChildrenOf(parentId: Int): List<TodoItem>

    @Update
    suspend fun update(todo: TodoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem)

    @Query("UPDATE TodoItem SET 'order' = :newOrder WHERE id = :id")
    suspend fun updateItemOrder(id: Int, newOrder: Int)

    @Query("UPDATE TodoItem SET 'expanded' = :expand WHERE id = :id")
    suspend fun expandItem(id: Int, expand: Boolean)

    @Query("DELETE FROM TodoItem WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Transaction
    suspend fun deleteItemAndDescendants (goalId: Int) {
        val children = getChildrenOf(goalId)
        for (child in children) {
            deleteItemAndDescendants(child.id)
        }
        deleteById(goalId)
    }

    @Transaction
    suspend fun updateItemsInTransaction(todos: List<TodoItem>) {
        todos.forEach { update(it) }
    }

}
