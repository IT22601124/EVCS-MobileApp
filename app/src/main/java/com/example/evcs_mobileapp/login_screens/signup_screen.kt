package com.example.evcs_mobileapp.login_screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcs_mobileapp.R

@Preview(showBackground = true)
@Composable
fun SignupScreen() {
    var firstName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        // Top image (replace with your asset)
        Box(
            modifier = Modifier
                .size(400.dp, 200.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.signupimage), // Add your logo to drawable
                contentDescription = "App Logo",
                modifier = Modifier.fillMaxSize()

            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name", fontSize = 12.sp) },
            modifier = Modifier.size(300.dp, 40.dp),
            shape = RoundedCornerShape(25.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email",fontSize = 12.sp) },
            modifier = Modifier.size(300.dp, 40.dp),
            shape = RoundedCornerShape(25.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password",fontSize = 12.sp) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.size(300.dp, 40.dp),
            shape = RoundedCornerShape(25.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password",fontSize = 12.sp) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.size(300.dp, 40.dp),
            shape = RoundedCornerShape(25.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Number",fontSize = 12.sp) },
            modifier = Modifier.size(300.dp, 40.dp),
            shape = RoundedCornerShape(25.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* Handle signup logic */ },
            modifier = Modifier
                .width(300.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Sign Up", fontSize = 14.sp)
        }



        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(300.dp)
                .padding(vertical = 16.dp)
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.Gray,
                thickness = 1.dp
            )
            Text(
                "Or",
                modifier = Modifier.padding(horizontal = 8.dp),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.Gray,
                thickness = 1.dp
            )
        }


        Button(
            onClick = { /* Handle Google login */ },
            modifier = Modifier
                .width(300.dp)
                .fillMaxWidth()
                .height(48.dp).border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(25.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.gicon), // Replace with your Google icon resource
                contentDescription = "Google Icon",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Login with Google", fontSize = 14.sp, color = Color.Black)
        }


    }
}