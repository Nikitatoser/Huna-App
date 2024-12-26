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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    chatId: String,
    userId: String,
    onNavigateBack: () -> Unit,
    navController: NavHostController
) {
    val messages = remember { mutableStateListOf<Message>() }
    val messageText = remember { mutableStateOf("") }
    val productInfo = remember { mutableStateOf<String?>(null) }

    val sellerName = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    val productId = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(chatId) {
        val db = FirebaseFirestore.getInstance()
        try {
            val chatDoc = db.collection("chats").document(chatId).get().await()
            productId.value = chatDoc.getString("productId")
        } catch (e: Exception) {
            productId.value = null
        }
    }


    LaunchedEffect(productId.value) {
        productId.value?.let {
            getProductInfo(it) { productDetails ->
                productInfo.value = productDetails
                isLoading.value = false
            }
        }
    }


    LaunchedEffect(productId.value) {
        productId.value?.let {
            val db = FirebaseFirestore.getInstance()
            try {
                val productDoc = db.collection("products").document(it).get().await()
                val ownerId = productDoc.getString("ownerId")
                if (ownerId != null) {
                    val userDoc = db.collection("users").document(ownerId).get().await()
                    sellerName.value = userDoc.getString("name") ?: "Unknown"
                } else {
                    sellerName.value = "Unknown"
                }
            } catch (e: Exception) {
                sellerName.value = "Error"
            }
        }
    }


    LaunchedEffect(chatId) {
        getMessages(chatId) { newMessages ->
            messages.clear()
            messages.addAll(newMessages)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1960AB))
                .clickable {

                    productId.value?.let {
                        navController.navigate("product_details/$it")
                    }
                }
                .padding(16.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1960AB))
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
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
                        text = "${productInfo.value ?: "No data"}",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${sellerName.value ?: "No data"}",
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
                        messageText.value = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1960AB),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(50.dp)
                    .wrapContentWidth(),
                elevation = ButtonDefaults.buttonElevation(8.dp)
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

fun getProductInfo(productId: String, onResult: (String?) -> Unit) {
    FirebaseFirestore.getInstance().collection("products").document(productId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null) {
                onResult(document.getString("name") ?: "Unknown item")
            } else {
                onResult("No data")
            }
        }
        .addOnFailureListener {
            onResult("Error")
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

    db.collection("messages")
        .document(chatId)
        .collection("chatMessages")
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("Chat", "Error fetching messages", e)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Message::class.java)
            } ?: emptyList()

            onMessagesReceived(messages)
        }
}

fun sendMessage(chatId: String, userId: String, messageText: String) {
    val db = FirebaseFirestore.getInstance()

    val message = mapOf(
        "senderId" to userId,
        "message" to messageText,
        "timestamp" to System.currentTimeMillis()
    )

    val messageRef = db.collection("messages")
        .document(chatId)
        .collection("chatMessages")
        .document()

    messageRef.set(message).addOnSuccessListener {
        Log.d("SendMessage", "Message sent successfully")

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
fun Long.formatToReadableTime(): String {
    val date = Date(this)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

