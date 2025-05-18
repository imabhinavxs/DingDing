package com.dingding.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onOrderClick: () -> Unit,
    onPlanSelected: (planName: String) -> Unit = {}
) {
    val db = Firebase.firestore
    var todaysMenu by remember { mutableStateOf<Map<String, Any>?>(null) }
    var subscriptionPlans by remember { mutableStateOf<Map<String, Map<String, Any>>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch data from Firestore
    LaunchedEffect(Unit) {
        try {
            val menuResult = db.collection("menus").document("today_special").get().await()
            val plansResult = db.collection("menus").document("subscription_plans").get().await()

            todaysMenu = menuResult.data
            subscriptionPlans = plansResult.data?.mapValues { it.value as Map<String, Any> }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load menu: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOrderClick,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Place Order")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Menu Section
                item {
                    Text(
                        "Today's Menu",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                todaysMenu?.let { menu ->
                    item {
                        MealCard(
                            title = "Lunch",
                            items = menu["lunch"].toString(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        MealCard(
                            title = "Dinner",
                            items = menu["dinner"].toString(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Subscription Plans Section
                item {
                    Text(
                        "Subscription Plans",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }

                subscriptionPlans?.let { plans ->
                    items(plans.entries.toList()) { (planName, planData) ->
                        SubscriptionPlanCard(
                            planName = planName.replaceFirstChar { it.uppercase() },
                            price = planData["price"] as Long,
                            meals = planData["meals"] as Long,
                            validityDays = planData["validity_days"] as Long,
                            onSelect = { onPlanSelected(planName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealCard(
    title: String,
    items: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = items,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    planName: String,
    price: Long,
    meals: Long,
    validityDays: Long,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = planName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹$price",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Meals", style = MaterialTheme.typography.labelMedium)
                    Text("$meals", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Validity", style = MaterialTheme.typography.labelMedium)
                    Text("$validityDays days", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Per Meal", style = MaterialTheme.typography.labelMedium)
                    Text("₹${price / meals}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}