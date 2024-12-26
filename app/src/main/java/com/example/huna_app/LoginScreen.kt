package com.example.huna_app

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@Composable
fun LoginScreen(navController: NavController) {
    val auth = Firebase.auth

    var emailState = remember { mutableStateOf("") }
    var passState = remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Welcome Back",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1960AB)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )



                OutlinedTextField(
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = passState.value,
                    onValueChange = { passState.value = it },
                    label = { Text(text = "Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (isPasswordVisible) Icons.Default.Lock else Icons.Default.Lock
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                Button(
                    onClick = { signIn(auth, emailState.value, passState.value, navController) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1960AB),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ClickableText(
                    text = AnnotatedString("REGISTRATION"),
                    style = TextStyle(
                        color = Color(0xFF9586A8),
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.SemiBold
                    ),
                    onClick = { navController.navigate("register") }
                )
            }
        }
    }
}

private fun signIn(auth: FirebaseAuth, email: String, password: String, navController: NavController){
    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
        if(it.isSuccessful){
            navController.navigate("home")
        }else{
            Log.d("MyLog", "SignIn: fail")
        }
    }
}