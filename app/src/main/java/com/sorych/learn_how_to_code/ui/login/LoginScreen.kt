import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.sorych.learn_how_to_code.R
import com.sorych.learn_how_to_code.ui.theme.DeepOceanBlue
import com.sorych.learn_how_to_code.ui.theme.SandyBeige

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegistrationClick: () -> Unit) {
    // State variables for inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = Firebase.auth

    Column (modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFA8EDEA), Color(0xFF2F9EB3), Color(0xFF003F5C))
            )
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(painter = painterResource(
            id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            modifier = Modifier.size(100.dp)
        )
        Text(text = "${stringResource(R.string.app_name)}", color = Color(0xFF003F5C), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))
        // Title

        // Email input field
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(0.6f),
            value = email,
            onValueChange = { email = it },
            label = {Text("Email", fontSize = 12.sp)},
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        // Space line
        Spacer(modifier = Modifier.height(4.dp))

        // Password input field
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(0.6f),
            value = password,
            onValueChange = { password = it },
            label = {Text("Password", fontSize = 12.sp)},
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        // Space line
        Spacer(modifier = Modifier.height(32.dp))
        //Login button
        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Please fill out all fields"
                    return@Button
                } else {
                    errorMessage = ""
                    // Call Firebase login
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Login successful
                                onLoginSuccess()
                            } else {
                                // Login failed
                                errorMessage = task.exception?.localizedMessage ?: "Login failed"
                            }}
                }
            },
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.6f),
            colors = ButtonDefaults.buttonColors(
                containerColor = SandyBeige,
                contentColor = DeepOceanBlue
            )
        ) {
            Text(text = "Login", fontSize = 16.sp)
        }
        // Space line
        Spacer(modifier = Modifier.height(8.dp))

        // Shows an error message if any
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        // Space line
        Spacer(modifier = Modifier.height(8.dp))

        // Navigate to Register
        TextButton(onClick = onRegistrationClick) {
            Text(text = "Don't have an account? Register", color = SandyBeige)
        }
    }
}