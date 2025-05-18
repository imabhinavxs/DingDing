package com.dingding.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

@Composable
fun OtpScreen(
    verificationId: String,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it },
            label = { Text("6-digit OTP") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (otp.length == 6) {
                        isLoading = true
                        verifyOtp(
                            verificationId = verificationId,
                            otp = otp,
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    } else {
                        errorMessage = "Please enter a valid 6-digit OTP"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify OTP")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text("Change Phone Number")
        }
    }
}

private fun verifyOtp(
    verificationId: String,
    otp: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val credential: PhoneAuthCredential =
        PhoneAuthProvider.getCredential(verificationId, otp)

    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
            onError("Verification failed: ${exception.message}")
        }
}
