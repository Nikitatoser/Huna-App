package com.example.huna_app.main_nav

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.huna_app.Product
import com.example.huna_app.ProductItem
import com.google.firebase.firestore.FirebaseFirestore

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
            .padding(16.dp)
    ) {
        Text(
            text = "Усі товари",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
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
                    ProductItem(
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
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        placeholder = { Text("Пошук товарів") },
        singleLine = true
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
            label = { Text("Категорія") },
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