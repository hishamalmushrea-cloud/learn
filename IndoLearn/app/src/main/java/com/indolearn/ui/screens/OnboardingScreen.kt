package com.indolearn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.indolearn.R
import com.indolearn.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(navController: NavController, viewModel: OnboardingViewModel) {
    var step by remember { mutableStateOf(0) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(step) {
        visible = false
        delay(100)
        visible = true
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val composition by rememberLottieComposition(
                LottieCompositionSpec.Url("https://assets3.lottiefiles.com/packages/lf20_q5pk6p1k.json") // Language learning animation placeholder
            )
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever,
            )

            if (step == 0) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(250.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (step) {
                            0 -> stringResource(R.string.welcome_title)
                            1 -> stringResource(R.string.level_question)
                            else -> stringResource(R.string.goal_question)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when (step) {
                            0 -> stringResource(R.string.welcome_subtitle)
                            1 -> stringResource(R.string.level_desc)
                            else -> stringResource(R.string.goal_desc)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200)) { it / 2 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        0 -> {
                            Button(
                                onClick = { step++ },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(stringResource(R.string.start_now), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        1 -> {
                            OutlinedButton(onClick = { step++ }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                                Text(stringResource(R.string.level_beginner))
                            }
                            OutlinedButton(onClick = { step++ }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                                Text(stringResource(R.string.level_basic))
                            }
                        }
                        2 -> {
                            OutlinedButton(
                                onClick = { 
                                    viewModel.completeOnboarding()
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }, 
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(stringResource(R.string.goal_travel))
                            }
                            OutlinedButton(
                                onClick = { 
                                    viewModel.completeOnboarding()
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }, 
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(stringResource(R.string.goal_work))
                            }
                        }
                    }
                }
            }
        }
    }
}
