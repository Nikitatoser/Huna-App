package com.example.huna_app

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private lateinit var auth: FirebaseAuth
@Composable
fun RegisterScreen (navController: NavController){

    val auth = Firebase.auth


    var emailState = remember {
        mutableStateOf("")
    }
    var passState = remember {
        mutableStateOf("")
    }
    var nameState = remember {
        mutableStateOf("")
    }
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(text = "Welcome back")
        OutlinedTextField(value = nameState.value, onValueChange = {
            nameState.value = it
        }, label = {
            Text(text = "Full name")
        })
        OutlinedTextField(value = emailState.value, onValueChange = {
            emailState.value = it
        }, label = {
            Text(text = "Email")
        })
        OutlinedTextField(value = passState.value, onValueChange = {
            passState.value = it
        }, label = {
            Text(text = "Password")
        })
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { signUp(auth, emailState.value, passState.value, navController) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE), // Колір фону кнопки
                contentColor = Color.White         // Колір тексту
            ),
            shape = RoundedCornerShape(12.dp),     // Закруглення кутів
            modifier = Modifier
                .height(50.dp)
                .width(200.dp),                    // Висота і ширина кнопки
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,           // Тінь кнопки
                pressedElevation = 4.dp            // Тінь при натисканні
            )
        ) {
            Text(
                text = "Sign up",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        ClickableText(
            text = AnnotatedString("LOGIN"),
            style = TextStyle(color = Color.Blue, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
            onClick = {
                navController.navigate("login")
            }
        )

    }


}

private fun signUp(auth: FirebaseAuth, email: String, password: String, navController: NavController){


    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
        if(it.isSuccessful){
            navController.navigate("home")
            Log.d("MyLog", "SignUp: success")
        }else{
            Log.d("MyLog", "SignUp: fail")
        }
    }
}