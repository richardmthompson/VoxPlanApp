package com.voxplanapp.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TodoRepository(private val todoDao: TodoDao) {

    // get specific todo
    fun getItemStream(id: Int): Flow<TodoItem?> = todoDao.getItem(id)

    // get all todo's
    fun getAllTodos(): Flow<List<TodoItem>> = todoDao.getAllTodos()

    fun getRootTodos() : List<TodoItem> = todoDao.getRootTodos()

    // get a list of items from a list of provided ids
    fun getItemsByIds(ids: List<Int>): Flow<List<TodoItem>> = todoDao.getItemsByIds(ids)

    suspend fun expandItem(todoId: Int, expand: Boolean) = todoDao.expandItem(todoId, expand)

    fun getChildrenOf(parentId: Int): List<TodoItem> = todoDao.getChildrenOf(parentId)

    // insert new todo
    suspend fun insert(todo: TodoItem) {
        todoDao.insert(todo)
        Log.d("TodoRepository", "Inserting todo: $todo")
    }

    // this is a toggle function - if the item is completed, it becomes uncompleted & vice versa.
    suspend fun completeItem(todoItem: TodoItem) {
        val updatedItem = if (todoItem.completedDate == null) {
            todoItem.copy(completedDate = LocalDate.now())
        } else {
            todoItem.copy(completedDate = null)
        }
        todoDao.update(updatedItem)
    }

    suspend fun updateItemsInTransaction(items: List<TodoItem>) {
        todoDao.updateItemsInTransaction(items)
    }

    // delete todo
    suspend fun deleteItemAndDescendents(todo: TodoItem) { todoDao.deleteItemAndDescendants(todo.id) }

    // update todo
    suspend fun updateItem(todo: TodoItem) {
        Log.d("repository","updateItem: ${todo.title} (${todo.id}), parent ${todo.parentId}, order ${todo.order}")
        todoDao.update(todo)
    }
}
