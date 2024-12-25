package com.example.huna_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AllChatScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val chats = remember { mutableStateListOf<Chat>() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        // Завантаження списку чатів користувача
        db.collection("chats")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error getting documents: $e")
                    return@addSnapshotListener
                }

                // Очищуємо попередній список чатів
                chats.clear()

                snapshot?.documents?.forEach { document ->
                    val chatId = document.id
                    val chat = document.toObject(Chat::class.java)

                    if (chat != null) {
                        // Завантаження останнього повідомлення та назви товару
                        scope.launch {
                            val lastMessage = db.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .await()
                                .documents
                                .firstOrNull()
                                ?.toObject(Message::class.java)

                            val productName = chat.productId?.let { productId ->
                                db.collection("products")
                                    .document(productId)
                                    .get()
                                    .await()
                                    .getString("name") ?: "Unknown product"
                            }

                            // Оновлення списку чатів
                            val updatedChat = chat.copy(
                                lastMessage = lastMessage?.message ?: "No messages",
                                timestamp = lastMessage?.timestamp ?: 0L,
                                productId = productName
                            )

                            chats.add(updatedChat)
                        }
                    }
                }
            }
    }

    // Інтерфейс списку чатів
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(chats) { chat ->
                ChatItem(chat = chat, navController = navController)
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, navController: NavHostController) {
    val otherUserId = chat.participants.firstOrNull { it != FirebaseAuth.getInstance().currentUser?.uid } ?: "Unknown user"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("chat/${chat.chatId}/${chat.productId}")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Chat with $otherUserId", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = chat.lastMessage ?: "", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Product: ${chat.productId}", fontSize = 14.sp, color = Color.DarkGray)
            }
            Text(text = formatTime(chat.timestamp), fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// Форматування часу
fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}



