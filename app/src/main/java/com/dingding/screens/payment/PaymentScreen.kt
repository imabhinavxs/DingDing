package com.dingding.screens.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dingding.R

@Composable
fun PaymentScreen(
    planName: String,
    price: Long,
    meals: Long,
    onPaymentDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Confirm Your Payment",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF333333)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF3E0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Plan: ${planName.replaceFirstChar { it.uppercase() }}", fontSize = 18.sp)
                Text(text = "Price: ₹$price", fontSize = 18.sp)
                Text(text = "Meals: $meals", fontSize = 18.sp)
            }
        }

        Text(
            text = "Scan the QR below to pay",
            style = MaterialTheme.typography.bodyLarge
        )

        // Replace this with your QR image (put it in `res/drawable`)
        Image(
            painter = painterResource(id = R.drawable.qr_code),
            contentDescription = "UPI QR Code",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Button(
            onClick = { onPaymentDone() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Payment Done", fontSize = 16.sp, color = Color.White)
        }
    }
}
