package com.example.huna_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ProductDetailScreen(productId: String?, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val product = remember { mutableStateOf<Product?>(null) }
    val isFavorite = remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val productId = productId

    // Check if the product is in favorites
    fun checkIfFavorite(userId: String, productId: String) {
        db.collection("favorites").document(userId).get()
            .addOnSuccessListener { document ->
                val favorite = document.toObject(Favorite::class.java)
                isFavorite.value = favorite?.productLinks?.contains(productId) == true
            }
            .addOnFailureListener {
                println("Error checking favorites")
            }
    }

    LaunchedEffect(productId) {
        productId?.let {
            db.collection("products").document(it)
                .get()
                .addOnSuccessListener { document ->
                    val productData = document.toObject(Product::class.java)
                    product.value = productData

                    // Check if the product is in favorites
                    checkIfFavorite(userId, it)
                }
        }
    }

    // Update favorites in Firestore
    fun updateFavorites(add: Boolean) {
        val favoritesRef = db.collection("favorites").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(favoritesRef)
            val favorite = snapshot.toObject(Favorite::class.java) ?: Favorite(userId)

            val updatedLinks = if (add) {
                product.value?.id?.let { favorite.productLinks + it } ?: favorite.productLinks
            } else {
                product.value?.id?.let { favorite.productLinks - it } ?: favorite.productLinks
            }

            transaction.set(favoritesRef, favorite.copy(productLinks = updatedLinks))
        }.addOnSuccessListener {
            isFavorite.value = add
        }.addOnFailureListener { e ->
            println("Error updating favorites: $e")
        }
    }

    if (product.value != null) {
        val productDetail = product.value!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Вирівнює елементи по сторонах
            ) {
                // Кнопка "Назад" зі стрілкою
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1960AB))
                        .clickable { navController.navigateUp() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Кнопка "Додати в обране" з серцем
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFavorite.value) Color(0xFF1960AB) else Color.LightGray)
                        .clickable { updateFavorites(!isFavorite.value) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.White
                    )
                }
            }


            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (!productDetail.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = productDetail.imageUrl,
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "No Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product Information
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Product Name
                Text(
                    text = productDetail.name,
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Price Card
                Card(
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF90B9F6)),

                ){
                    Text(
                        text = "${productDetail.price}$",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Address
                Text(
                    text = productDetail.address,
                    fontSize = 20.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description Section
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = productDetail.description ?: "No description available",
                            fontSize = 20.sp,
                            color = Color.Black

                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (productId != null) {
                            createChatIfNotExists(productId) { chatId ->
                                navController.navigate("chat/$chatId")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF90B9F6)
                    )
                ) {
                    Text(
                        "MESSAGE",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

            }
        }
    } else {
        // Loading Indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


fun createChatIfNotExists(productId: String, onChatCreated: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
        val chatsRef = db.collection("chats")
        val productsRef = db.collection("products") // Колекція товарів
        val userId = currentUser.uid

        // Отримуємо власника товару
        productsRef.document(productId).get()
            .addOnSuccessListener { productSnapshot ->
                val ownerId = productSnapshot.getString("ownerId") // Власник товару

                if (ownerId != null) {
                    // Шукаємо існуючий чат
                    chatsRef
                        .whereEqualTo("productId", productId)
                        .whereArrayContains("participants", userId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                // Якщо чат вже існує, повертаємо його ID
                                val chatId = querySnapshot.documents[0].id
                                onChatCreated(chatId)
                            } else {
                                // Створюємо новий чат
                                val newChat = hashMapOf(
                                    "productId" to productId,
                                    "participants" to listOf(userId, ownerId), // Додаємо обох учасників
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                chatsRef.add(newChat)
                                    .addOnSuccessListener { documentReference ->
                                        onChatCreated(documentReference.id)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Chat", "Failed to create chat: ${e.message}")
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Chat", "Error fetching chats: ${e.message}")
                        }
                } else {
                    Log.e("Chat", "Owner ID not found for product: $productId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Chat", "Error fetching product: ${e.message}")
            }
    }
}













