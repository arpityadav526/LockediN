package com.lockedin.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.lockedin.R
import com.lockedin.core.components.PinDialog
import com.lockedin.core.components.ToolCard
import com.lockedin.navigation.Routes

data class ToolItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    onUnlock: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var showPinDialog by remember { mutableStateOf(false) }

    val tools = remember {
        listOf(
            ToolItem("AI Chat", Icons.Default.Chat, Routes.AI_CHAT),
            ToolItem("Calculator", Icons.Default.Calculate, Routes.CALCULATOR),
            ToolItem("Dictionary", Icons.Default.MenuBook, Routes.DICTIONARY),
            ToolItem("Timer", Icons.Default.Timer, Routes.TIMER),
            ToolItem("Notes", Icons.Default.EditNote, Routes.NOTES),
            ToolItem("Converter", Icons.Default.SwapHoriz, Routes.CONVERTER),
            ToolItem("Formulas", Icons.Default.Functions, Routes.FORMULAS),
            ToolItem("Files", Icons.Default.Folder, Routes.FILES),
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "LockediN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            actions = {
                // Logo — secret 5-tap target
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = "LockediN",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // No ripple — secret gesture
                        ) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime > 3000L) tapCount = 0
                            tapCount++
                            lastTapTime = now
                            if (tapCount >= 5) {
                                tapCount = 0
                                showPinDialog = true
                            }
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Study stats row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StudyStatItem(
                    icon = Icons.Default.Schedule,
                    value = "${uiState.totalStudyMinutes}",
                    label = "Minutes Studied"
                )
                StudyStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${uiState.pomodoroCycles}",
                    label = "Pomodoros"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section title
        Text(
            text = "Study Tools",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Tool grid (2×4)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(tools) { tool ->
                ToolCard(
                    title = tool.title,
                    icon = tool.icon,
                    onClick = {
                        navController.navigate(tool.route)
                    },
                    modifier = Modifier.aspectRatio(1.2f)
                )
            }
        }
    }

    // PIN dialog for secret unlock
    if (showPinDialog) {
        PinDialog(
            onConfirm = { enteredPin ->
                val isCorrect = viewModel.verifyPin(enteredPin)
                if (isCorrect) {
                    onUnlock()
                    navController.navigate(Routes.SETTINGS)
                }
                showPinDialog = false
            },
            onDismiss = { showPinDialog = false }
        )
    }
}

@Composable
private fun StudyStatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
