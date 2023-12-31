package com.example.todolist.data.remote.source

import com.example.todolist.data.remote.model.TodoListContentDTO
import com.example.todolist.data.remote.service.FirebaseService
import com.example.todolist.domain.entity.TodoListContentModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SourceRemoteImpl(
    firestore: FirebaseFirestore
) : FirebaseService {
    private val collectionName = "todo_list_content"
    private val query = firestore.collection(collectionName)

    override suspend fun getTodoList(filter: String): List<TodoListContentDTO> {
        val data = query.get().await().map {
            TodoListContentDTO(
                description = it.data["description"]?.toString().orEmpty(),
                isCompleted = it.data["isCompleted"]?.toString()?.toBoolean() ?: false,
                timestamp = it.data["timestamp"]?.toString().orEmpty(),
                itemHashCode = it.data["itemHashCode"]?.toString()?.toIntOrNull() ?: 0,
                users = it.data["users"] as? List<String> ?: emptyList(),
                referenceId = it.id
            )
        }
        return if (filter.isEmpty()) {
            data
        } else data.filter {
            it.description.contains(filter) || it.description.lowercase().contains(filter)
        }
    }

    override suspend fun deleteTodoListContent(todoListContent: TodoListContentModel) {
        val referenceId = todoListContent.referenceId ?: return
        query.document(referenceId).delete().await()
    }

    override suspend fun insertTodoListContent(todoListContent: TodoListContentModel) {
        val content = hashMapOf(
            "description" to todoListContent.description,
            "isCompleted" to todoListContent.isCompleted,
            "timestamp" to todoListContent.timestamp,
            "itemHashCode" to todoListContent.itemHashCode,
            "users" to todoListContent.users
        )
        val referenceId = query.add(content).await().id
        content["referenceId"] = referenceId
        query.document(referenceId).update(content).await()
    }

    override suspend fun updateTodoListContent(todoListContent: TodoListContentModel) {
        val referenceId = todoListContent.referenceId ?: return
        val content = hashMapOf(
            "description" to todoListContent.description,
            "isCompleted" to todoListContent.isCompleted,
            "timestamp" to todoListContent.timestamp,
            "itemHashCode" to todoListContent.itemHashCode,
            "users" to todoListContent.users,
            "referenceId" to referenceId
        )
        query.document(referenceId).update(content).await()
    }
}