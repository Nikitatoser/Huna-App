package com.example.huna_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController

@Composable
fun AllChatScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var chats by remember { mutableStateOf<List<Map<String, Any>>?>(null) }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            db.collection("chats")
                .whereArrayContains("participants", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    chats = querySnapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
                }
                .addOnFailureListener { e ->
                    chats = emptyList()
                    Log.e("Chats", "Failed to fetch chats: ${e.message}")
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Your Chats",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1960AB),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                chats == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                chats!!.isEmpty() -> {
                    Text(
                        text = "No chats found",

                        color = Color.LightGray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chats!!) { chat ->
                            ChatItem(chat, db) { chatId ->
                                navController.navigate("chat/$chatId")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: Map<String, Any>, db: FirebaseFirestore, onClick: (String) -> Unit) {
    val chatId = chat["id"] as? String ?: return
    val productId = chat["productId"] as? String ?: "Unknown Product"
    var productName by remember { mutableStateOf<String?>(null) }
    var lastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        db.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                productName = document.getString("name")
            }

        db.collection("chats").document(chatId).collection("chats")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val message = snapshot.documents[0]
                    lastMessage = message.getString("text")
                }
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(chatId) }
            .shadow(2.dp, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5))
    )  {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productName ?: "Loading...",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,

                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1960AB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    }
}


