package com.example.huna_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    chatId: String,
    productId: String,
    userId: String,
    onNavigateBack: () -> Unit,
    navController: NavHostController
) {
    val messages = remember { mutableStateListOf<Message>() }
    val messageText = remember { mutableStateOf("") }
    val productInfo = remember { mutableStateOf<String?>(null) }

    val sellerName = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    // Завантаження даних про товар
    LaunchedEffect(productId) {
        getProductInfo(productId) { productDetails ->
            productInfo.value = productDetails
            isLoading.value = false
        }
    }

    // Завантаження імені продавця
    LaunchedEffect(productId) {
        val db = FirebaseFirestore.getInstance()
        try {
            val productDoc = db.collection("products").document(productId).get().await()
            val ownerId = productDoc.getString("ownerId")
            if (ownerId != null) {
                val userDoc = db.collection("users").document(ownerId).get().await()
                sellerName.value = userDoc.getString("name") ?: "Невідомий продавець"
            } else {
                sellerName.value = "Невідомий продавець"
            }
        } catch (e: Exception) {
            sellerName.value = "Помилка завантаження"
        }
    }

    // Завантаження повідомлень
    LaunchedEffect(chatId) {
        getMessages(chatId) { newMessages ->
            messages.clear()
            messages.addAll(newMessages)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Верхній блок з кнопкою назад, назвою товару і продавцем
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1960AB)) // Колір фону
                .clickable {
                    // Перевірка на наявність productId перед переходом
                    if (productId.isNotEmpty()) {
                        navController.navigate("product_details/${productId}")
                    }
                }
                .padding(16.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)) // Закруглені нижні кути
        ){
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)) // Округлення
                        .background(Color(0xFF1960AB)) // Фон кнопки
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp)) // Білий бордер
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${productInfo.value ?: "Немає даних"}",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${sellerName.value ?: "Немає даних"}",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Список повідомлень
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(messages) { message ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = if (userId == message.senderId) Alignment.End else Alignment.Start
                ) {
                    MessageItem(
                        message = message,
                        isCurrentUser = userId == message.senderId
                    )
                    Text(
                        text = message.timestamp.formatToReadableTime(),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color.Gray
                    )
                }
            }
        }

        // Поле для введення повідомлення
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Поле для введення повідомлення
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText.value,
                    onValueChange = { messageText.value = it },
                    modifier = Modifier
                        .weight(1f),
                    label = { Text("Your message") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (messageText.value.isNotEmpty()) {
                            sendMessage(chatId, userId, messageText.value)
                            messageText.value = "" // Очищуємо поле після відправлення
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1960AB),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp), // Округлення для кнопки
                    modifier = Modifier
                        .height(50.dp) // Висота кнопки
                        .wrapContentWidth(), // Кнопка має фіксовану ширину, що відповідає її вмісту
                    elevation = ButtonDefaults.buttonElevation(8.dp) // Тінь для кнопки
                ) {
                    Text(
                        text = "Send",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }



        }
    }
}

fun onNavigateToProduct(productId: String, navController: NavController) {
    navController.navigate("product_details/$productId")
}


// Функція для отримання даних про товар
fun getProductInfo(productId: String, onResult: (String?) -> Unit) {
    // Симуляція отримання інформації з бази даних
    FirebaseFirestore.getInstance().collection("products").document(productId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null) {
                onResult(document.getString("name") ?: "Невідомий товар")
            } else {
                onResult("Немає даних")
            }
        }
        .addOnFailureListener {
            onResult("Помилка завантаження даних")
        }
}


@Composable
fun MessageItem(message: Message, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isCurrentUser) Color(0xFF90CAF9) else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = message.message,
                color = if (isCurrentUser) Color.White else Color.Black,
                fontSize = 16.sp
            )
        }
    }
}





fun getMessages(chatId: String, onMessagesReceived: (List<Message>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("messages") // Основна колекція з повідомленнями
        .document(chatId) // Документ з конкретним чатом
        .collection("chatMessages") // Колекція повідомлень у чаті
        .orderBy("timestamp", Query.Direction.ASCENDING) // Сортування за часом
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("Chat", "Error fetching messages", e)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Message::class.java)
            } ?: emptyList()

            onMessagesReceived(messages) // Відправляємо повідомлення у список
        }
}

fun sendMessage(chatId: String, userId: String, messageText: String) {
    val db = FirebaseFirestore.getInstance()

    // Створюємо об'єкт повідомлення
    val message = mapOf(
        "senderId" to userId,
        "message" to messageText,
        "timestamp" to System.currentTimeMillis()
    )

    // Додаємо повідомлення у відповідний чат
    val messageRef = db.collection("messages")
        .document(chatId)
        .collection("chatMessages")
        .document()

    messageRef.set(message).addOnSuccessListener {
        Log.d("SendMessage", "Message sent successfully")

        // Оновлюємо останнє повідомлення у документі чату
        val chatRef = db.collection("chats").document(chatId)
        chatRef.update(
            mapOf(
                "lastMessage" to messageText,
                "timestamp" to System.currentTimeMillis()
            )
        ).addOnFailureListener { e ->
            Log.e("SendMessage", "Error updating chat metadata: $e")
        }
    }.addOnFailureListener { e ->
        Log.e("SendMessage", "Error sending message: $e")
    }
}



@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isCurrentUser) Color(0xFF1960AB) else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.message,
                    color = if (isCurrentUser) Color.White else Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = formatTimestamp(message.timestamp),
                    color = if (isCurrentUser) Color(0xFFB0C4DE) else Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
fun Long.formatToReadableTime(): String {
    val date = Date(this)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault()) // Формат години:хвилини
    return format.format(date)
}
fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}
