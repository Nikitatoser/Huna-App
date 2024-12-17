package com.example.huna_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ProductDetailScreen(productId: String?, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val product = remember { mutableStateOf<Product?>(null) }
    val isFavorite = remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val customColor = Color(0xFFF5F5F5)

    // Перевірка, чи товар вже в обраному
    fun checkIfFavorite(userId: String, productId: String) {
        db.collection("favorites").document(userId).get()
            .addOnSuccessListener { document ->
                val favorite = document.toObject(Favorite::class.java)
                isFavorite.value = favorite?.productLinks?.contains(productId) == true
            }
            .addOnFailureListener {
                println("Помилка перевірки обраного")
            }
    }

    LaunchedEffect(productId) {
        productId?.let {
            db.collection("products").document(it)
                .get()
                .addOnSuccessListener { document ->
                    val productData = document.toObject(Product::class.java)
                    product.value = productData

                    // Перевірка, чи товар вже в обраному
                    checkIfFavorite(userId, it)
                }
        }
    }



    // Оновлення обраних товарів у Firestore
    fun updateFavorites(add: Boolean) {
        val favoritesRef = db.collection("favorites").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(favoritesRef)
            val favorite = snapshot.toObject(Favorite::class.java) ?: Favorite(userId)

            val updatedLinks = if (add) {
                // Додаємо товар, якщо його ID не null
                product.value?.id?.let { favorite.productLinks + it } ?: favorite.productLinks
            } else {
                // Видаляємо товар, якщо його ID не null
                product.value?.id?.let { favorite.productLinks - it } ?: favorite.productLinks
            }

            transaction.set(favoritesRef, favorite.copy(productLinks = updatedLinks))
        }.addOnSuccessListener {
            println("Обрані товари успішно оновлено!")
            isFavorite.value = add
        }.addOnFailureListener { e ->
            println("Помилка оновлення обраного: $e")
        }
    }


    if (product.value != null) {
        val productDetail = product.value!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .padding(16.dp)
                .background(customColor)
        ) {
            // Кнопка "Назад"
            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(text = "Назад")
            }

            // Фото товару
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
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
                    Text(text = "Немає фото", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Інформація про товар
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Назва товару
                Text(
                    text = productDetail.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val customColor1 = Color(0xFF90B9F6) // Ціна та адреса
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(customColor1)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "${productDetail.price}$",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = productDetail.address,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Блок опису
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Опис товару",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = productDetail.description ?: "Опис відсутній",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                // Кнопка "Додати в обране"
                Button(
                    onClick = {
                        // Оновлення стану кнопки та Firestore
                        updateFavorites(!isFavorite.value)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                       if (isFavorite.value) Color.Green else Color.Gray
                    )
                ) {
                    Text(
                        text = if (isFavorite.value) "У обраному" else "Додати в обране",
                        color = Color.White
                    )
                }
            }
        }
    } else {
        // Показуємо індикатор завантаження, якщо дані ще не завантажені
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


