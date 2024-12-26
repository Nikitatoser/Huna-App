package com.example.huna_app.main_nav

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.huna_app.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("I sell") }
    var price by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    address = document.getString("address") ?: ""
                }
                .addOnFailureListener {
                    address = ""
                }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

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
            Text(
                text = "Add item",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1960AB)
            )
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                Image(
                    painter = rememberImagePainter(photoUri),
                    contentDescription = "Uploaded Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Photo", tint = Color.Gray)
                }
            }
        }

        // Поле введення заголовку
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Поле введення опису
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth(),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )
        // Поле введення адреси
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier
                .fillMaxWidth(),
            maxLines = 2,
            shape = RoundedCornerShape(12.dp)
        )


        // Поле для введення ціни та категорії
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            DropdownMenuField(
                selectedOption = category,
                options = listOf("I need", "I sell"),
                onOptionSelected = { category = it },
            )
        }

        // Кнопка "Додати товар"
        Button(
            onClick = {
                errorMessage = validateInputs(title, description, price, address)
                if (errorMessage.isEmpty() && currentUser != null) {
                    saveProductToFirestore(
                        title = title,
                        description = description,
                        price = price,
                        category = category,
                        address = address,
                        deliveryType = "Pick up",
                        photoUri = photoUri,
                        db = db,
                        currentUser = currentUser!!,
                        onSuccess = {

                            title = ""
                            description = ""
                            price = ""
                            address = ""
                            photoUri = null
                            errorMessage = "Product added successfully."
                        },
                        onError = { error ->
                            errorMessage = error
                        }
                    )
                } else if (currentUser == null) {
                    errorMessage = "You must login"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                Color(0xFF90B9F6),
            )
        ) {
            Text("Add item",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,

            )
        }
    }

}


fun validateInputs(title: String, description: String, price: String, address: String): String {
    return when {
        title.isEmpty() -> "Title is required."
        description.isEmpty() -> "Description is required."
        price.isEmpty() || price.toDoubleOrNull() == null -> "Enter a valid price."
        else -> ""
    }
}




@Composable
fun UploadPhotoButton(photoUri: Uri?, onPhotoSelected: (Uri?) -> Unit) {
    Button(onClick = {

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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .menuAnchor()

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
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        // Валідація даних
        if (title.isBlank()) {
            onError("Title is required.")
            return
        }
        if (description.isBlank()) {
            onError("Description is required.")
            return
        }
        if (price.toDoubleOrNull() == null) {
            onError("Enter a valid price.")
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
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError("Error updating product ID: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onError("Error adding product: ${e.message}")
            }
    } catch (e: Exception) {
        onError("Unexpected error: ${e.message}")
    }
}
