package com.example.huna_app.main_nav

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.huna_app.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun MainScreen(db: FirebaseFirestore, navController: NavHostController) {
    // Список всіх товарів
    val allProductsList = remember { mutableStateListOf<Product>() }
    // Список відфільтрованих товарів
    val filteredProductsList = remember { mutableStateListOf<Product>() }
    // Стан для пошукового запиту
    var searchQuery by remember { mutableStateOf("") }
    // Стан для вибору категорії
    var selectedCategory by remember { mutableStateOf("Усі") }
    // Список категорій
    val categories = listOf("Усі", "Продаю", "Шукаю")

    // Запит до Firestore для отримання всіх товарів
    LaunchedEffect(Unit) {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                allProductsList.clear()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    allProductsList.add(product)
                }
                // Оновлюємо відфільтрований список після завантаження
                filteredProductsList.clear()
                filteredProductsList.addAll(allProductsList)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching all products", exception)
            }
    }

    // Оновлення фільтрації при зміні пошукового запиту або категорії
    LaunchedEffect(searchQuery, selectedCategory) {
        filteredProductsList.clear()
        filteredProductsList.addAll(
            allProductsList.filter { product ->
                (selectedCategory == "Усі" || product.category == selectedCategory) &&
                        product.name.contains(searchQuery, ignoreCase = true)
            }
        )
    }

    // Інтерфейс головної сторінки
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "HUNA",
            fontSize = 35.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1960AB),
            modifier = Modifier.padding(bottom = 1.dp)
        )

        // Компонент пошуку
        SearchBar(
            query = searchQuery,
            onQueryChange = { newQuery -> searchQuery = newQuery }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Вибір категорії
        CategoryDropdown(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { newCategory -> selectedCategory = newCategory }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredProductsList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredProductsList) { product ->
                    ProductItems(
                        product = product,
                        navController = navController // Передаємо навігатор
                    )
                }
            }
        } else {
            // Виводимо повідомлення, якщо немає результатів
            Text(
                text = if (searchQuery.isEmpty() && selectedCategory == "Усі")
                    "Немає доступних товарів"
                else
                    "Нічого не знайдено",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun ProductItems(product: Product, navController: NavHostController) {
    // Стан для збереження імені продавця
    var sellerName by remember { mutableStateOf("Завантаження...") }

    // Завантаження імені з Firestore
    LaunchedEffect(product.ownerId) {
        val db = FirebaseFirestore.getInstance()
        try {
            val document = db.collection("users").document(product.ownerId).get().await()
            sellerName = document.getString("name") ?: "Невідомий продавець"
        } catch (e: Exception) {
            sellerName = "Помилка завантаження"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("product_details/${product.id}") // Переходимо на сторінку деталей товару
            },

        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5))
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Зображення товару
            val imageModifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray)

            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Зображення товару",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = imageModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "No Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Інформація про товар
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.address,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Продавець: $sellerName",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Ціна
            Box(
                modifier = Modifier
                    .background(Color(0xFF90B9F6), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${product.price} $",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
