package com.indolearn.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.utils.NotificationHelper

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        NotificationHelper.createChannel(context)
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("الإعدادات", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        Text("الوضع الداكن: يتبع النظام")
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("تذكير يومي")
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    if (it) {
                        NotificationHelper.showDailyReminder(context)
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("سيظهر تذكير يومي للمراجعة", style = MaterialTheme.typography.bodySmall)
    }
}