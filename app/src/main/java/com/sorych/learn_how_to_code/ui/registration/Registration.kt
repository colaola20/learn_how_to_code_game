package com.sorych.learn_how_to_code.ui.registration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.sorych.learn_how_to_code.R
import com.sorych.learn_how_to_code.ui.theme.SandyBeige
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

// ---------- Validation Helpers ----------

// Check if email matches Android’s email pattern
fun isValidEmail(email: String) : Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// Check if name has between 3 and 30 characters
fun isValidName(name: String) : Boolean {
    return name.length in 3..30
}

fun isUnder16(dobString: String): Boolean {
    return try {
        // Normalize input: replace slashes with dashes
        val normalized = dobString.replace("/", "-")

        if (normalized.length != 10) return false // Need full date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dob = LocalDate.parse(normalized, formatter)
        val today = LocalDate.now()
        val age = Period.between(dob, today).years
        age < 16
    } catch (e: Exception) {
        false // invalid date format
    }
}


@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit = {}, onLoginClick: () -> Unit) {
    // State variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("parent") }
    var parentEmail by remember { mutableStateOf("") }

    val auth = Firebase.auth
    val db = Firebase.firestore




    Column (modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFA8EDEA), Color(0xFF2F9EB3), Color(0xFF003F5C))
            )
        )
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //         Logo
        Image(painter = painterResource(
            id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            modifier = Modifier.size(128.dp)
        )
        Text(text = "${stringResource(R.string.app_name)}", color = Color(0xFF003F5C), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            // First name input field
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = {Text("First Name", fontSize = 12.sp)},
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )


            // Last name input field
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = {Text("Last Name", fontSize = 12.sp)},
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        // Space line
        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            // DOB input field
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = dob,
                onValueChange = {
                    dob = it
                    role = if (isUnder16(dob)) "child" else "parent"
                                },
                label = {Text("Date of birth", fontSize = 12.sp)},
                placeholder = {Text("YYYY-MM-DD", fontSize = 12.sp)},
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
            )

            Text(
                text = "Role: $role",
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )

        }


        // Space line
        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            // Email input field
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = email,
                onValueChange = { email = it },
                label = {Text("Email", fontSize = 12.sp)},
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Password input field
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                value = password,
                onValueChange = { password = it },
                label = {Text("Password", fontSize = 12.sp)},
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }

        if (role == "child") {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(0.8f),
                value = parentEmail,
                onValueChange = { password = it },
                label = {Text("Parent's email", fontSize = 12.sp)},
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }


        // Space line
        Spacer(modifier = Modifier.height(24.dp))
        // Register button
        Button(
            onClick = {
                // Fields validation
                when {
                    email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty() -> {
                        errorMessage = "Please fill out all fields"
                    }
                    !isValidEmail(email) -> {
                        errorMessage = "Please enter a valid email"
                    }
                    !isValidName(firstName) -> {
                        errorMessage = "First name must be 3 to 30 characters"
                    }
                    !isValidName(lastName) -> {
                        errorMessage = "Last name must be 3 to 30 characters"
                    }
                    role == "child" && (parentEmail.isEmpty() || !isValidEmail(parentEmail)) -> {
                        errorMessage = "Please enter valid parent's email"
                    }
                    else -> {
                        errorMessage = ""
                        // Register user in Firebase Auth
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onRegisterSuccess()

                                    // Example of saving user to Firestore
                                    val userId = auth.currentUser?.uid ?: ""
                                    val user = hashMapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "dob" to dob,
                                        "email" to email
                                    )
                                    db.collection("users").document(userId)
                                        .set(user)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) { print("all good") }}
                                        .addOnFailureListener { e ->
                                            errorMessage =
                                                e.localizedMessage ?: "Failed to save user info"
                                        }
                                } else {
                                    // Login failed
                                    errorMessage =
                                        task.exception?.localizedMessage ?: "Registration failed"
                                }
                            }
                    }
                }
            },
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(
                containerColor = SandyBeige,
                contentColor = Color(0xFF003F5C)
            )
        ) {
            Text(text = "Register", fontSize = 16.sp)
        }

        // Show error message
        if (errorMessage.isNotEmpty()) {
            // Space line
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        // Space line
        Spacer(modifier = Modifier.height(4.dp))

        // // Back to login
        TextButton(onClick = onLoginClick) {
            Text(text = "Already have an account? Login", color = SandyBeige)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onRegisterSuccess = {}, onLoginClick = {})
}

