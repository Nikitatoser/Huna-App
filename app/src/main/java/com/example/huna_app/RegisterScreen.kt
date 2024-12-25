package com.example.huna_app

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private lateinit var auth: FirebaseAuth
@Composable
fun RegisterScreen(navController: NavController) {

    val auth = Firebase.auth

    val dateOfBirthState = remember { mutableStateOf("") }
    var emailState = remember { mutableStateOf("") }
    var passState = remember { mutableStateOf("") }
    var passState1 = remember { mutableStateOf("") }
    var nameState = remember { mutableStateOf("") }

    // Оформлення для фону
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Виведення зображення як фон
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = null,  // Опис для доступності
            modifier = Modifier.fillMaxSize()  // Робить зображення фоном, що покриває весь Box
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Виправлення
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Account",
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1960AB)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                label = { Text(text = "Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)

            )

            OutlinedTextField(
                value = dateOfBirthState.value,
                onValueChange = { input ->
                    // Форматування дати
                    val digitsOnly = input.filter { it.isDigit() }
                    val formattedDate = buildString {
                        for (i in digitsOnly.indices) {
                            append(digitsOnly[i])
                            if ((i == 1 || i == 3) && i != digitsOnly.length - 1) append("/")
                        }
                    }
                    if (formattedDate.length <= 10) dateOfBirthState.value = formattedDate
                },
                label = { Text(text = "Date of Birth (dd/mm/yyyy)") },
                placeholder = { Text(text = "dd/mm/yyyy") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = { Text(text = "Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = passState.value,
                onValueChange = { passState.value = it },
                label = { Text(text = "Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = passState1.value,
                onValueChange = { passState1.value = it },
                label = { Text(text = "Repeat Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { signUp(auth, emailState.value, passState.value, passState1.value, nameState.value, dateOfBirthState.value, navController) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1960AB), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(50.dp)
                    .width(200.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
            ) {
                Text(text = "Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            ClickableText(
                text = AnnotatedString("LOGIN"),
                style = TextStyle(
                    color = Color(0xFF9586A8),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.SemiBold
                ),
                onClick = { navController.navigate("login") }
            )
        }
    }
    }
}

private fun signUp(auth: FirebaseAuth, email: String, password: String, password1: String, name: String, dateOfBirth: String, navController: NavController){
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
        if(it.isSuccessful){
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && name.isNotBlank() && dateOfBirth.isNotBlank()) {
                if (password == password1) {
                    saveUserToFirestore(name, dateOfBirth, FirebaseFirestore.getInstance(), user)
                    navController.navigate("home")
                    Log.d("MyLog", "SignUp: success")
                }
                else{

                    Log.d("MyLog", "SignUp: fail")
                }
            }
        }else{
            Log.d("MyLog", "SignUp: fail")
        }
    }
}


fun saveUserToFirestore(
    name: String,
    date: String,
    db: FirebaseFirestore,
    user: FirebaseUser // Передаємо поточного користувача для отримання його UID
) {
    try {
        // Валідація імені
        if (name.isBlank()) {
            Log.e("SaveUser", "Name cannot be empty")
            return
        }
        if (date.isBlank()) {
            Log.e("SaveUser", "Name cannot be empty")
            return
        }

        // Створення об'єкта User з UID як ID
        val userData = User(
            id = user.uid,  // Використовуємо UID з FirebaseAuth
            name = name,
            age = date,
            address = null.toString()
        )

        // Додавання користувача у Firestore з UID як ID документа
        db.collection("users")
            .document(user.uid)  // Використовуємо UID як ID для документа
            .set(userData)
            .addOnSuccessListener {
                Log.d("Firestore", "User added successfully with UID: ${user.uid}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding user", e)
            }
    } catch (e: Exception) {
        Log.e("SaveUser", "Error saving user", e)
    }
}


