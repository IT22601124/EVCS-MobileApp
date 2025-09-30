@file:Suppress("PreviewAnnotationInFunctionWithParameters")

package com.example.evcs_mobileapp.screens.walkthrough

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcs_mobileapp.R
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.layout.ContentScale

@Composable
fun Walkthrough1Content(
    onNext: (() -> Unit)? = null,
    onSkip: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.walkthrough1_background), // Add walkthrough_2.png to drawable
            contentDescription = "Reservation walkthrough",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        // Optional gradient overlay (uncomment if you want a tint)
        /*
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xAAE5F9F6), // Semi-transparent teal
                            Color(0xAAB2F7EF),
                            Color(0xAAFFFFFF)
                        )
                    )
                )
        )
        */
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Box(
                modifier = Modifier
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(24.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Easily find EV charging stations around you",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onSkip?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E7EB)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip", color = Color(0xFF22C55E))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onNext?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp, 6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB))
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun walkthrough_1(navController: NavController) {
    Walkthrough1Content(
        onNext = { navController.navigate("walkthrough_2") },
        onSkip = { /* navController.navigate("login_screen") */ }
    )
}

@Preview(showBackground = true)
@Composable
fun Walkthrough1Preview() {
    Walkthrough1Content()
}
