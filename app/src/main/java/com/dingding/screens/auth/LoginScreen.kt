package com.dingding.screens.auth

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dingding.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(
    onOtpSent: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.dingding_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            placeholder = { Text("e.g. +917905954521") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val cleanedNumber = phoneNumber.replace(" ", "")
                    if (cleanedNumber.startsWith("+") && cleanedNumber.length >= 12) {
                        if (activity != null) {
                            isLoading = true
                            sendOtp(
                                phone = cleanedNumber,
                                activity = activity,
                                onCodeSent = { verificationId ->
                                    isLoading = false
                                    onOtpSent(verificationId)
                                },
                                onError = {
                                    isLoading = false
                                    errorMessage = it
                                }
                            )
                        } else {
                            errorMessage = "Activity context is not available"
                        }
                    } else {
                        errorMessage = "Invalid format. Example: +917905954521"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send OTP")
            }
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/////////////////////////////////////////////////////////
// ✅ Paste this below the @Composable function above //
/////////////////////////////////////////////////////////

fun sendOtp(
    phone: String,
    activity: Activity,
    onCodeSent: (String) -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            auth.signInWithCredential(credential)
                .addOnSuccessListener { /* Auto login, optional */ }
                .addOnFailureListener { e ->
                    onError("Auto-verification failed: ${e.message}")
                }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            onError("Verification failed: ${e.message}")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            onCodeSent(verificationId)
        }
    }

    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phone) // Already E.164 format
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callbacks)
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}
