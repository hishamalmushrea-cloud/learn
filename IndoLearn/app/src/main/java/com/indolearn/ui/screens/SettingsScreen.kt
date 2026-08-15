package com.indolearn.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.indolearn.utils.NotificationHelper

/** إعدادات حقيقية محفوظة؛ لا Switch يعود إلى true عند كل دخول. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("user_settings", 0) }
    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean("daily_reminder", false))
    }
    var permissionDenied by remember { mutableStateOf(false) }

    fun enableReminder() {
        notificationsEnabled = true
        permissionDenied = false
        prefs.edit().putBoolean("daily_reminder", true).apply()
        NotificationHelper.scheduleDailyReminder(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) enableReminder()
        else {
            notificationsEnabled = false
            permissionDenied = true
        }
    }

    LaunchedEffect(Unit) { NotificationHelper.createChannel(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text("المظهر", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("الوضع الداكن يتبع إعداد النظام", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("تذكير المراجعة اليومي", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "إشعار واحد كل 24 ساعة، ويعمل حتى بعد إعادة تشغيل التطبيق.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (!enabled) {
                            notificationsEnabled = false
                            prefs.edit().putBoolean("daily_reminder", false).apply()
                            NotificationHelper.cancelDailyReminder(context)
                        } else if (
                            Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else enableReminder()
                    }
                )
            }
            if (permissionDenied) {
                Text(
                    "لم يُمنح إذن الإشعارات. يمكنك تفعيله لاحقاً من إعدادات النظام.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
