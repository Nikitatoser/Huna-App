package com.example.huna_app.main_nav

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.huna_app.Product
import com.example.huna_app.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var userName by remember { mutableStateOf("Loading...") }
    var userImage by remember { mutableStateOf("") }

    LaunchedEffect(currentUser?.uid) {
        val db = FirebaseFirestore.getInstance()
        currentUser?.let { user ->
            try {
                val document = db.collection("users").document(user.uid).get().await()
                userName = document.getString("name") ?: "Name not available"
                userImage = document.getString("profileImageUrl") ?: ""
            } catch (e: Exception) {
                userName = "Error loading"
                userImage = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                text = "Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1960AB)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(25))
                .background(Color.LightGray),

            contentAlignment = Alignment.Center

        ) {
            if (userImage.isNotEmpty()) {
                AsyncImage(
                    model = userImage,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile Icon",
                    modifier = Modifier.size(55.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1C3D5A)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ProfileButton(
            text = "Account",
            onClick = { navController.navigate("account_settings") },
            icon = Icons.Default.AccountCircle
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileButton(
            text = "Your items",
            onClick = { navController.navigate("user_items") },
            icon = Icons.Default.List
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileButton(
            text = "Settings",
            onClick = { navController.navigate("all_settings") },
            icon = Icons.Default.Settings
        )
    }
}

@Composable
fun ProfileButton(text: String, onClick: () -> Unit, icon: ImageVector) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            Color(0xFF1960AB),
            contentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}




@Composable
fun UsersItems(navController: NavHostController) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    val products = remember { mutableStateListOf<Product>() }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    // Завантаження товарів
    LaunchedEffect(user) {
        user?.let {
            db.collection("products")
                .whereEqualTo("ownerId", it.uid)
                .get()
                .addOnSuccessListener { result ->
                    products.clear()
                    for (document in result) {
                        val product = document.toObject(Product::class.java).apply {
                            id = document.id
                        }
                        products.add(product)
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                text = "Your items",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1960AB)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (products.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(products) { product ->
                    ProductItem(
                        product = product,
                        navController = navController,
                        onEdit = { productToEdit = it },
                        onDelete = { productId ->
                            db.collection("products").document(productId).delete()
                                .addOnSuccessListener {
                                    products.removeIf { it.id == productId }
                                }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "У вас немає товарів",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (productToEdit != null) {
        EditProductDialog(
            product = productToEdit!!,
            onDismiss = { productToEdit = null },
            onSave = { updatedProduct ->
                db.collection("products").document(updatedProduct.id!!).set(updatedProduct)
                    .addOnSuccessListener {
                        val index = products.indexOfFirst { it.id == updatedProduct.id }
                        if (index != -1) {
                            products[index] = updatedProduct
                        }
                        productToEdit = null
                    }
            }
        )
    }
}


@Composable
fun ProductItem(
    product: Product,
    navController: NavHostController,
    onEdit: (Product) -> Unit,
    onDelete: (String) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("product_details/${product.id}") }, // Перехід на сторінку деталей товару
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
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
                        imageVector = Icons.Default.Clear,
                        contentDescription = "No Image",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Інформація про товар
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.address,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))
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

            Column(){
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background( Color(0xFF1960AB))
                        .clickable {    onEdit(product)   },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Create,
                        contentDescription = "Favorite",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background( Color(0xFF1960AB))
                        .clickable {    onDelete(product.id ?: "")  },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Favorite",
                        tint = Color.White
                    )
                }
            }


        }
    }
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name ?: "") }
    var description by remember { mutableStateOf(product.description ?: "") }
    var price by remember { mutableStateOf(product.price?.toString() ?: "") }
    var address by remember { mutableStateOf(product.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Product") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedPrice = price.toDoubleOrNull()
                if (parsedPrice != null) {
                    onSave(
                        product.copy(
                            name = name,
                            description = description,
                            address = address,
                            price = parsedPrice
                        )
                    )
                } else {
                    // Можна додати логіку для відображення помилки, якщо ціна некоректна
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun AccountSettingsScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var userName by remember { mutableStateOf("Loading...") }
    var userImage by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("Not set") }

    var showChangeAddressDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val userId = auth.currentUser?.uid

    LaunchedEffect(currentUser?.uid) {
        val db = FirebaseFirestore.getInstance()
        currentUser?.let { user ->
            try {
                val document = db.collection("users").document(user.uid).get().await()
                userName = document.getString("name") ?: "Name not available"
                userImage = document.getString("profileImageUrl") ?: ""
                address = document.getString("address") ?: "Not set"
            } catch (e: Exception) {
                userName = "Error loading"
                userImage = ""
                address = "Not set"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                text = "Account settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1960AB)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(25))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (userImage.isNotEmpty()) {
                AsyncImage(
                    model = userImage,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile Icon",
                    modifier = Modifier.size(55.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1C3D5A)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Address: $address",
            fontSize = 20.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileButton(
            text = "Change Name",
            onClick = { showChangeNameDialog = true },
            icon = Icons.Default.AccountCircle
        )
        Spacer(modifier = Modifier.height(16.dp))

        ProfileButton(
            text = "Change Address",
            onClick = { showChangeAddressDialog = true },
            icon = Icons.Default.Home
        )
        Spacer(modifier = Modifier.height(16.dp))

        ProfileButton(
            text = "Change Password",
            onClick = { showChangePasswordDialog = true },
            icon = Icons.Default.Lock
        )

        if (showChangeAddressDialog) {
            ChangeAddressDialog(onDismiss = { showChangeAddressDialog = false }, onAddressChange = { newAddress ->
                address = newAddress
                updateUserAddress(userId, newAddress)
            })
        }

        if (showChangeNameDialog) {
            ChangeNameDialog(onDismiss = { showChangeNameDialog = false }, onNameChange = { newName ->
                userName = newName
                updateUserName(userId, newName)
            })
        }

        if (showChangePasswordDialog) {
            ChangePasswordDialog(onDismiss = { showChangePasswordDialog = false })
        }
    }
}

@Composable
fun ChangeAddressDialog(onDismiss: () -> Unit, onAddressChange: (String) -> Unit) {
    var newAddress by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Address") },
        text = {
            Column {
                Text("Enter your new address:")
                TextField(value = newAddress, onValueChange = { newAddress = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAddressChange(newAddress)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangeNameDialog(onDismiss: () -> Unit, onNameChange: (String) -> Unit) {
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Name") },
        text = {
            Column {
                Text("Enter your new name:")
                TextField(value = newName, onValueChange = { newName = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onNameChange(newName)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit) {
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                Text("Enter your new password:")
                TextField(value = newPassword, onValueChange = { newPassword = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                updatePassword(newPassword)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun updateUserAddress(userId: String?, newAddress: String) {
    val db = FirebaseFirestore.getInstance()
    userId?.let {
        db.collection("users").document(it).update("address", newAddress)
    }
}

fun updateUserName(userId: String?, newName: String) {
    val db = FirebaseFirestore.getInstance()
    userId?.let {
        db.collection("users").document(it).update("name", newName)
    }
}

fun updatePassword(newPassword: String) {
    val auth = FirebaseAuth.getInstance()
    auth.currentUser?.updatePassword(newPassword)
}





fun deleteAccount(auth: FirebaseAuth, userId: String, navController: NavController) {
    // Видалити всі товари користувача
    deleteUserProducts(userId) { isProductDeleted ->
        if (isProductDeleted) {
            // Товари успішно видалені, тепер видаляємо дані користувача з Firestore
            deleteUserDataFromFirestore(userId) { isUserDataDeleted ->
                if (isUserDataDeleted) {
                    // Якщо дані користувача також видалені, видаляємо акаунт
                    auth.currentUser?.delete()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            } else {
                                Log.e("DeleteAccount", "Failed to delete account: ${task.exception}")
                            }
                        }
                } else {
                    Log.e("DeleteAccount", "Failed to delete user data from Firestore")
                }
            }
        } else {
            Log.e("DeleteAccount", "Failed to delete user products")
        }
    }
}

fun deleteUserProducts(userId: String, onComplete: (Boolean) -> Unit) {
    val productsCollection = FirebaseFirestore.getInstance().collection("products")

    productsCollection.whereEqualTo("ownerId", userId)
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val products = task.result
                if (products != null && !products.isEmpty) {
                    for (product in products) {
                        product.reference.delete()
                            .addOnSuccessListener {
                                if (product == products.last()) {
                                    onComplete(true)
                                }
                            }
                            .addOnFailureListener {
                                Log.e("DeleteAccount", "Failed to delete product: ${product.id}")
                                onComplete(false)
                            }
                    }
                } else {
                    onComplete(true)
                }
            } else {
                Log.e("DeleteAccount", "Failed to fetch products: ${task.exception}")
                onComplete(false)
            }
        }
}

fun deleteUserDataFromFirestore(userId: String, onComplete: (Boolean) -> Unit) {
    val usersCollection = FirebaseFirestore.getInstance().collection("users")

    usersCollection.document(userId).delete()
        .addOnSuccessListener {
            onComplete(true)
        }
        .addOnFailureListener { exception ->
            Log.e("DeleteAccount", "Error deleting user data: ${exception.localizedMessage}")
            onComplete(false)
        }
}





@Composable
fun AllSettingsScreen(navController: NavHostController){

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var userName by remember { mutableStateOf("Loading...") }
    var userImage by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    val userId = auth.currentUser?.uid

    LaunchedEffect(currentUser?.uid) {
        val db = FirebaseFirestore.getInstance()
        currentUser?.let { user ->
            try {
                val document = db.collection("users").document(user.uid).get().await()
                userName = document.getString("name") ?: "Name not available"
                userImage = document.getString("profileImageUrl") ?: ""
            } catch (e: Exception) {
                userName = "Error loading"
                userImage = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                text = "Account settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1960AB)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(25))
                .background(Color.LightGray),

            contentAlignment = Alignment.Center

        ) {
            if (userImage.isNotEmpty()) {
                AsyncImage(
                    model = userImage,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile Icon",
                    modifier = Modifier.size(55.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1C3D5A)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ProfileButton(
            text = "Logout",
            onClick = {
                auth.signOut()
                navController.navigate("login") { // Перехід на екран логіну
                    popUpTo("profile") { inclusive = true }
                }},
            icon = Icons.Default.ExitToApp
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileButton(
            text = "Delete account",
            onClick = { showDialog = true },
            icon = Icons.Default.Delete
        )


        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Are you sure you want to delete your account?") },
                text = { Text("This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        // Викликаємо функцію видалення акаунта та його даних
                        if (userId != null) {
                            deleteAccount(auth, userId, navController)
                        }
                        showDialog = false
                    }) {
                        Text("Yes, Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}


