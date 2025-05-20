package com.dingding.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dingding.screens.auth.LoginScreen
import com.dingding.screens.auth.OtpScreen
import com.dingding.screens.menu.MenuScreen
import com.dingding.screens.payment.PaymentScreen
import com.dingding.screens.user.UserInfoScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onOtpSent = { verificationId ->
                    navController.navigate("otp/$verificationId")
                }
            )
        }

        composable("otp/{verificationId}") { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            OtpScreen(
                verificationId = verificationId,
                onLoginSuccess = { navController.navigate("menu") },
                onBack = { navController.popBackStack("login", inclusive = false) }
            )
        }

        composable("menu") {
            MenuScreen(
                onOrderClick = { navController.navigate("user_info") }
            )
        }

        composable("user_info") {
            UserInfoScreen(
                onContinueToPayment = { navController.navigate("payment") }
            )
        }

        composable("payment") {
            PaymentScreen(
                planName = "premium",
                price = 3150,
                meals = 30,
                onPaymentDone = {
                    navController.popBackStack("menu", false)
                }
            )

        }
    }
}
