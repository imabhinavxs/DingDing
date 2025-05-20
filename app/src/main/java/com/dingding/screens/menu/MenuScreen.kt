package com.dingding.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onOrderClick: () -> Unit,
    onPlanSelected: (planName: String, price: Long, meals: Long) -> Unit = { _, _, _ -> }
) {
    var todaysMenu by remember { mutableStateOf<Map<String, Any>?>(null) }
    var subscriptionPlans by remember { mutableStateOf<Map<String, Map<String, Any>>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    suspend fun loadData() {
        try {
            val db = Firebase.firestore
            val menuResult = db.collection("menus").document("today_special").get().await()
            val plansResult = db.collection("menus").document("subscription_plans").get().await()

            todaysMenu = menuResult.data
            subscriptionPlans = plansResult.data?.mapValues { it.value as Map<String, Any> }
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = when (e) {
                is FirebaseFirestoreException -> "Network error. Please check your connection"
                else -> "Failed to load menu: ${e.localizedMessage}"
            }
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    fun refresh() {
        isRefreshing = true
        coroutineScope.launch {
            loadData()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Today's Menu", color = Color(0xFF333333))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFEF6E4)
                ),
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFFFA824C)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOrderClick,
                containerColor = Color(0xFFFA824C),
                contentColor = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Place Order")
            }
        }
    ) { padding ->
        when {
            isLoading && !isRefreshing -> FullScreenLoading()
            errorMessage != null -> ErrorMessage(
                message = errorMessage!!,
                onRetry = { refresh() }
            )
            else -> MenuContent(
                todaysMenu = todaysMenu,
                subscriptionPlans = subscriptionPlans,
                padding = padding,
                onPlanSelected = onPlanSelected
            )
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MenuContent(
    todaysMenu: Map<String, Any>?,
    subscriptionPlans: Map<String, Map<String, Any>>?,
    padding: PaddingValues,
    onPlanSelected: (planName: String, price: Long, meals: Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        todaysMenu?.let { menu ->
            item {
                MealCard(
                    title = "Lunch",
                    items = parseMenuItems(menu["lunch"]?.toString())
                )
            }
            item {
                MealCard(
                    title = "Dinner",
                    items = parseMenuItems(menu["dinner"]?.toString())
                )
            }
        }

        item {
            Text(
                "Subscription Plans",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
                color = Color(0xFF333333)
            )
        }

        subscriptionPlans?.let { plans ->
            items(plans.entries.toList()) { (planName, planData) ->
                SubscriptionPlanCard(
                    planName = planName.replaceFirstChar { it.uppercase() },
                    price = (planData["price"] as? Long) ?: 0L,
                    meals = (planData["meals"] as? Long) ?: 0L,
                    validityDays = (planData["validity_days"] as? Long) ?: 0L,
                    onSelect = {
                        onPlanSelected(
                            planName,
                            (planData["price"] as? Long) ?: 0L,
                            (planData["meals"] as? Long) ?: 0L
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MealCard(title: String, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEEBCB)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            items.forEach { item ->
                Text(
                    text = "• $item",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SubscriptionPlanCard(
    planName: String,
    price: Long,
    meals: Long,
    validityDays: Long,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDFFFE0)),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = planName,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF1A3C40),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("₹$price", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A3C40))
            Text("Meals: $meals", fontSize = 14.sp, color = Color.DarkGray)
            Text("Validity: $validityDays days", fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

fun parseMenuItems(data: String?): List<String> {
    return data?.split(",")?.map { it.trim() } ?: emptyList()
}

@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
