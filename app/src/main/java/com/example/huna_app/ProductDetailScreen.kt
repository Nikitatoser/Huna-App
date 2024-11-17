package com.example.huna_app

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


@Composable
fun ProductDetailScreen(productId: String?) {
    val db = FirebaseFirestore.getInstance()
    val product = remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(productId) {
        productId?.let {
            db.collection("products").document(it)
                .get()
                .addOnSuccessListener { document ->
                    val productData = document.toObject(Product::class.java)
                    product.value = productData
                }
        }
    }

    if (product.value != null) {
        val productDetail = product.value!!
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = productDetail.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Price: ${productDetail.price}", fontSize = 18.sp)
            Text(text = "Description: ${productDetail.description}", fontSize = 16.sp)
            Text(text = "Address: ${productDetail.address}", fontSize = 16.sp)
            Text(text = "Delivery Type: ${productDetail.deliveryType}", fontSize = 16.sp)
        }
    } else {
        CircularProgressIndicator()
    }
}
