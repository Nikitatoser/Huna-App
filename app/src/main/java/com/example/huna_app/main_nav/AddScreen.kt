package com.example.huna_app.main_nav

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.huna_app.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddScreen() {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Продаю") }
    var price by remember { mutableStateOf("") }
    var deliveryType by remember { mutableStateOf("Самовивіз") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Поле для завантаження фото
        Text(text = "Завантажити фото:")
        UploadPhotoButton(photoUri) { newUri -> photoUri = newUri }

        // Поле для назви товару
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Назва") },
            modifier = Modifier.fillMaxWidth()
        )

        // Поле для опису товару
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Опис") },
            modifier = Modifier.fillMaxWidth()
        )

        // Вибір категорії: "Шукаю" або "Продаю"
        Text(text = "Категорія:")
        Row {
            RadioButton(
                selected = category == "Продаю",
                onClick = { category = "Продаю" }
            )
            Text(text = "Продаю", modifier = Modifier.padding(start = 8.dp))

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = category == "Шукаю",
                onClick = { category = "Шукаю" }
            )
            Text(text = "Шукаю", modifier = Modifier.padding(start = 8.dp))
        }

        // Поле для ціни
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Ціна") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Поле для опису товару
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        // Вибір типу доставки
        Text(text = "Тип доставки:")
        DropdownMenuField(
            selectedOption = deliveryType,
            options = listOf("Самовивіз", "Доставка"),
            onOptionSelected = { deliveryType = it }
        )

        // Кнопка для підтвердження
        Button(
            onClick = {
                if (currentUser != null) {
                    saveProductToFirestore(
                        title = title,
                        description = description,
                        price = price,
                        category = category,
                        address = address,
                        deliveryType = deliveryType,
                        photoUri = photoUri,
                        db = db,
                        currentUser = currentUser
                    )
                } else {
                    Log.e("AddScreen", "User is not logged in")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати товар")
        }
    }
}


@Composable
fun UploadPhotoButton(photoUri: Uri?, onPhotoSelected: (Uri?) -> Unit) {
    Button(onClick = {
        // Логіка для вибору фото
    }) {
        Text(if (photoUri != null) "Фото завантажено" else "Вибрати фото")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuField(selectedOption: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Тип доставки") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


fun saveProductToFirestore(
    title: String,
    description: String,
    price: String,
    category: String,
    address: String,
    deliveryType: String,
    photoUri: Uri?,
    db: FirebaseFirestore,
    currentUser: FirebaseUser,
) {
    try {
        // Валідація даних
        if (title.isBlank() || description.isBlank() || price.toDoubleOrNull() == null) {
            Log.e("SaveProduct", "Invalid product data")
            return
        }

        val imageUrl = photoUri?.toString() ?: ""
        val product = Product(
            id = "", // ID буде згенеровано Firestore
            name = title,
            description = description,
            price = price.toDouble(),
            address = address,
            deliveryType = deliveryType,
            addDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            available = true, // За замовчуванням
            category = category,
            imageUrl = imageUrl,
            ownerId = currentUser.uid
        )

        db.collection("products")
            .add(product)
            .addOnSuccessListener { documentReference ->
                val productId = documentReference.id
                documentReference.update("id", productId)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Product ID updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating product ID", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding product", e)
            }
    } catch (e: Exception) {
        Log.e("SaveProduct", "Error saving product", e)
    }
}