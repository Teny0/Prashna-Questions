package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Host
import com.example.data.JournalEntry
import com.example.data.SessionAnalysis
import com.example.ui.PrashnaViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RatingGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextInkDark
import com.example.ui.theme.TextSandLight
import com.example.ui.theme.TextMutedGray
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.TextStyle
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PrashnaApp()
            }
        }
    }
}

@Composable
fun PrashnaApp(viewModel: PrashnaViewModel = viewModel()) {
    val isCallActive by viewModel.isCallActive.collectAsStateWithLifecycle()
    val isPostCallAnalysisActive by viewModel.isPostCallAnalysisActive.collectAsStateWithLifecycle()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Screen Router
        if (isCallActive) {
            LiveAudioCallingRoom(viewModel = viewModel)
        } else {
            DashboardScreen(viewModel = viewModel)
        }

        // Overlays
        if (isPostCallAnalysisActive) {
            PostCallReflectionDialog(viewModel = viewModel)
        }
    }
}

@Composable
fun DashboardScreen(viewModel: PrashnaViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PrashnaHeader(viewModel = viewModel)
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "Browse Hosts") },
                    label = { Text("Browse", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("tab_browse")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "My Mandala") },
                    label = { Text("Mandala", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("tab_mandala")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.School, contentDescription = "Host Academy") },
                    label = { Text("Academy", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("tab_academy")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Dns, contentDescription = "Architecture") },
                    label = { Text("Blueprints", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("tab_blueprints")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            modifier = Modifier.padding(innerPadding),
            label = "tab_transition"
        ) { tab ->
            when (tab) {
                0 -> BrowseHostsTab(viewModel = viewModel)
                1 -> MyMandalaTab(viewModel = viewModel)
                2 -> HostAcademyTab(viewModel = viewModel)
                3 -> ArchitectureTab()
            }
        }
    }
}

@Composable
fun PrashnaHeader(viewModel: PrashnaViewModel) {
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "प्रश्न",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRASHNA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Truth is My God • The Universe is My Country",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontStyle = FontStyle.Italic
                )
            }

            // Credits display
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .clickable { viewModel.addCredits(50.0) }
                    .testTag("wallet_balance_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = "Wallet balance",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("%.1f cr", walletBalance),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add credits",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ======================= TAB 1: BROWSE HOSTS =======================
@Composable
fun BrowseHostsTab(viewModel: PrashnaViewModel) {
    val hostsList by viewModel.hosts.collectAsStateWithLifecycle()
    var selectedHostForCall by remember { mutableStateOf<Host?>(null) }
    var inquiryTopicInput by remember { mutableStateOf("") }
    
    // Preset philosophical topics for quick selection
    val presetTopics = listOf(
        "Deconstructing the Ego & Witness Consciousness",
        "The Threshold of Absolute Reality (Vedanta)",
        "Logic, Perception, and Overcoming Dogmatic Assumptions",
        "Socratic Dialogue: What is True Intellectual Humility?",
        "Emptiness, Impermanence, and Non-Attachment"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("browse_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Hero Branding Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFFE57C23).copy(alpha = 0.15f),
                                radius = 240.dp.toPx(),
                                center = Offset(size.width * 0.9f, size.height * 0.2f)
                            )
                        }
                        .padding(20.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ANONYMOUS VOICE PORTAL",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Pay for Wisdom, Not Noise.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Engage in rigorous, live anonymous audio dialogues with certified philosophical guides to expand self-awareness.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Instant Matching Button
                        Button(
                            onClick = {
                                if (hostsList.isNotEmpty()) {
                                    val randomHost = hostsList.random()
                                    val randomTopic = presetTopics.random()
                                    viewModel.startCall(randomHost, randomTopic)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("instant_match_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = "Instant Pair")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instant Socratic Match", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Verified Acharyas & Philosophical Hosts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (hostsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Hosts list cards
        items(hostsList, key = { it.id }) { host ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("host_card_${host.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = host.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = host.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                if (host.isVerified) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Guide",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = RatingGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${host.rating}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Wisdom Index",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Wisdom: ${host.wisdomScore}%",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Pricing Pill
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${host.pricingPerMinute} cr/min",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = host.bio,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    // Specialties Chips flow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        host.specialties.split(",").take(3).forEach { specialty ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = specialty.trim(),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { selectedHostForCall = host },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_call_btn_${host.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = "Call")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Initiate Anonymous Dialogue", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal dialogue to input/choose inquiry topic
    selectedHostForCall?.let { host ->
        Dialog(onDismissRequest = { selectedHostForCall = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("inquiry_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Set Inquiry Topic",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Select or type the philosophical theme you wish to deconstruct with ${host.name}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Quick choose topics
                    Text("Suggested Streams:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    presetTopics.forEach { topic ->
                        Card(
                            onClick = { inquiryTopicInput = topic },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (inquiryTopicInput == topic) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = topic,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inquiryTopicInput,
                        onValueChange = { inquiryTopicInput = it },
                        placeholder = { Text("Or formulate your custom question...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_topic_input"),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { selectedHostForCall = null }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                        }
                        Button(
                            onClick = {
                                val finalTopic = inquiryTopicInput.ifBlank { "Unconditional Self-Inquiry" }
                                viewModel.startCall(host, finalTopic)
                                selectedHostForCall = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("confirm_start_call_btn")
                        ) {
                            Text("Start Voice Session")
                        }
                    }
                }
            }
        }
    }
}

// ======================= TAB 2: PERSONAL MANDALA =======================
@Composable
fun MyMandalaTab(viewModel: PrashnaViewModel) {
    val entries by viewModel.journalEntries.collectAsStateWithLifecycle()
    val pastSessions by viewModel.sessions.collectAsStateWithLifecycle()
    val earnedBadges by viewModel.badges.collectAsStateWithLifecycle()

    var showAddJournalDialog by remember { mutableStateOf(false) }
    var journalTitleInput by remember { mutableStateOf("") }
    var journalContentInput by remember { mutableStateOf("") }
    var journalCategoryInput by remember { mutableStateOf("Self-Inquiry") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("mandala_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Badges Section
        item {
            Text(
                text = "Your Spiritual Badges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (earnedBadges.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No spiritual badges earned yet. Complete your first socratic assessment or dialogue to unlock wisdom indicators.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .height(130.dp)
                        .testTag("badges_grid"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(earnedBadges) { badge ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (badge.icon) {
                                        "explore" -> Icons.Default.Explore
                                        "hearing" -> Icons.Default.Hearing
                                        "psychology" -> Icons.Default.Psychology
                                        "edit_note" -> Icons.Default.EditNote
                                        "verified_user" -> Icons.Default.VerifiedUser
                                        else -> Icons.Default.WorkspacePremium
                                    },
                                    contentDescription = badge.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = badge.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = badge.description,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Journal Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Learning Journal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = { showAddJournalDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("add_journal_btn")
                ) {
                    Icon(Icons.Default.Create, contentDescription = "Write")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Write Note", fontSize = 12.sp)
                }
            }
        }

        // Journal entries list
        if (entries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your journal is blank. Writing down insights from dialogues is a verified way to internalize wisdom.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(entries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = entry.category.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteJournal(entry) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.content,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(entry.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        // Past completed sessions
        item {
            Text(
                text = "Dialogue History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (pastSessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No voice sessions recorded yet. Dialogue history shows your past philosophical inquiries, cost, and AI metrics.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(pastSessions) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Dialogue with ${session.hostName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format("%.1f cr deducted", session.cost),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = session.summary,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Quality Bars Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Curiosity", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text("${session.curiosityScore}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Listening", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text("${session.listeningScore}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Humility", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text("${session.respectScore}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Duration", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(viewModel.formatDuration(session.durationSeconds), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogue to create raw journal notes
    if (showAddJournalDialog) {
        Dialog(onDismissRequest = { showAddJournalDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("add_journal_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Write Insight",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = journalTitleInput,
                        onValueChange = { journalTitleInput = it },
                        label = { Text("Title (e.g. 'Witnessing the ego')") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_title_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Category drop-down choice simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Self-Inquiry", "Ethics", "Logic", "Vedanta").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (journalCategoryInput == cat) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { journalCategoryInput = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = if (journalCategoryInput == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = journalContentInput,
                        onValueChange = { journalContentInput = it },
                        label = { Text("Reflections / Socratic answers...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("journal_content_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showAddJournalDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                        }
                        Button(
                            onClick = {
                                viewModel.saveJournalEntry(
                                    title = journalTitleInput,
                                    category = journalCategoryInput,
                                    content = journalContentInput
                                )
                                journalTitleInput = ""
                                journalContentInput = ""
                                showAddJournalDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("confirm_add_journal_btn")
                        ) {
                            Text("Save Note")
                        }
                    }
                }
            }
        }
    }
}

// ======================= TAB 3: HOST ACADEMY =======================
@Composable
fun HostAcademyTab(viewModel: PrashnaViewModel) {
    val showExamResult by viewModel.showExamResult.collectAsStateWithLifecycle()
    val examPassed by viewModel.examPassed.collectAsStateWithLifecycle()
    val currentQuestionIdx by viewModel.examQuestionIndex.collectAsStateWithLifecycle()
    val answers by viewModel.examAnswers.collectAsStateWithLifecycle()

    val currentQuestion = viewModel.examQuestions[currentQuestionIdx]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("academy_tab")
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Prashna Host Academy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "To maintain high standards, we require hosts to pass multiple examinations on Gita, Upanishads, Nyaya logic, Socratic inquiry, and active listening. Earn your certified badge and become an active host.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!showExamResult) {
            // Display active MCQ test
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentQuestion.topic,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Question ${currentQuestionIdx + 1} of ${viewModel.examQuestions.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Options list
                    items(currentQuestion.options.size) { optionIdx ->
                        val optionText = currentQuestion.options[optionIdx]
                        val isSelected = answers[currentQuestionIdx] == optionIdx

                        Card(
                            onClick = { viewModel.answerExamQuestion(currentQuestionIdx, optionIdx) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("option_${currentQuestionIdx}_$optionIdx"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optionText,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Navigation Actions
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { viewModel.prevExamQuestion() },
                                enabled = currentQuestionIdx > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), contentColor = MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Previous")
                            }

                            if (currentQuestionIdx == viewModel.examQuestions.size - 1) {
                                Button(
                                    onClick = { viewModel.submitExam() },
                                    enabled = answers.size == viewModel.examQuestions.size,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("submit_exam_btn")
                                ) {
                                    Text("Submit Assessment", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.nextExamQuestion() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Next Question")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Exam Results screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (examPassed) SuccessGreen else MaterialTheme.colorScheme.error
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                if (examPassed) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (examPassed) Icons.Default.WorkspacePremium else Icons.Default.Cancel,
                            contentDescription = "Status",
                            tint = if (examPassed) SuccessGreen else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (examPassed) "Assessment Passed!" else "Assessment Incomplete",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (examPassed) SuccessGreen else MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (examPassed) {
                            "Congratulations! You scored 100% on the Prashna Board of Acharya qualification. The 'Acharya Certification' badge is now displayed on your Mandala. You are authorized to accept live audio consultations."
                        } else {
                            "To ensure deep philosophical awareness, we require 100% accuracy. Study Upanishadic logic, Socratic method, and Buddhism to retake the test."
                        },
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.resetExam() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (examPassed) SuccessGreen else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("retake_or_continue_btn")
                    ) {
                        Text(if (examPassed) "Take Another Assessment" else "Retake Assessment")
                    }
                }
            }
        }
    }
}

// ======================= TAB 4: ARCHITECTURE & PLAN =======================
@Composable
fun ArchitectureTab() {
    var selectedPlanSubTab by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("blueprints_tab")
    ) {
        // Inner Tab selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Schema", "Monetization", "Security", "Roadmap").forEachIndexed { index, name ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selectedPlanSubTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedPlanSubTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedPlanSubTab == index) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedPlanSubTab) {
                    0 -> { // Schema / DB / Architecture
                        item {
                            Text("Database Schema & Tech Stack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("The Prashna marketplace utilizes SQLite/Room for client local caching and offline Socratic journaling, paired with a robust Cloud Spanner distributed database on GCP.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("1. Schema: HOSTS Table", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("- id: INT (PRIMARY KEY AUTO_GENERATE)\n- name: VARCHAR(100)\n- specialties: TEXT (Comma-separated specialties)\n- rating: DOUBLE\n- pricing_per_minute: DOUBLE\n- wisdom_score: INT\n- trust_score: INT\n- bio: TEXT\n- is_verified: BOOLEAN", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                        item {
                            Text("2. Schema: JOURNALS Table", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("- id: INT (PRIMARY KEY)\n- title: VARCHAR(200)\n- content: TEXT\n- category: VARCHAR(50) (e.g., Logic, Vedanta)\n- timestamp: BIGINT (millis)\n- host_name: VARCHAR(100) (optional link)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                        item {
                            Text("3. Schema: SESSIONS Table", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("- id: INT (PRIMARY KEY)\n- host_name: VARCHAR(100)\n- duration_seconds: INT\n- cost: DOUBLE\n- timestamp: BIGINT\n- summary: TEXT\n- listening_score: INT\n- curiosity_score: INT\n- respect_score: INT", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                        item {
                            Text("4. Real-time Audio Backend", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("WebRTC audio channels routing through Coturn TURN servers, with audio streams encrypted end-to-end. AI microservices evaluate listening parameters via local VAD (Voice Activity Detection) on device to respect privacy.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    1 -> { // Monetization Model
                        item {
                            Text("Monetization & Economy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Prashna implements a dual-sided value exchange where users pay for intellectual depth, and hosts earn money based on rigorous academic qualifications.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("• Per-Minute Billing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Users pay per-minute (pricing defined by individual certified hosts). Platform takes a flat 15% commission to cover encryption and WebRTC routing, delivering 85% directly to certified guides.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("• Premium Subscriptions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Bronze/Gold tier subscriptions (e.g. 19.99 cr/month) unlocking access to private live webinars, monthly tipping boosts, and direct offline socratic text journals.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("• Corporate Socratic Seminars", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Enterprise licensing for critical-thinking workshops, active listening assessments, and executive perspective-taking circles guided by verified philosophy scholars.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    2 -> { // Security & Privacy
                        item {
                            Text("E2E Security & Complete Anonymity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Prashna is built with a zero-knowledge trust framework. Self-inquiry requires absolute psychological safety.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("• Dynamic Pitch-Shifting Audio Masking", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Real-time on-device voice masking altering human pitch and tone formats to prevent vocal identity leaks, with user consent.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("• Zero Call Records Policy", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Audio streams are strictly ephemeral and processed on the fly. No raw audio logs are stored on cloud servers. High-level text summaries are generated in-memory via Gemini's safe API context window, immediately wiped after analysis.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("• Cryptographic Wallet Security", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Billing tokens and micropayments are calculated on immutable ledgers using HMAC-SHA256 signatures to verify authenticity.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    3 -> { // MVP Roadmap & Scaling
                        item {
                            Text("MVP Roadmap & Growth Strategy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        item {
                            Text("Phase 1: Seed & Verification (Month 1-3)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Launch qualification exams across Sanskrit/Socratic academic circles. Secure first 100 certified philosophy PhDs/monks. Build core encrypted WebRTC audio channel on Android.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("Phase 2: Alpha Match (Month 4-6)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Open beta for students and spiritual seekers. Integrate Gemini real-time socratic hints. Refine credit purchase loop and local Room-based journaling.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        item {
                            Text("Phase 3: The Great Discourse (Month 7-12)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Global launch. Introduce multilingual support (Sanskrit, Hindi, Tamil, Greek, Japanese). Partner with universities to introduce critical thinking workshops globally.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

// ======================= LIVE CALLING ROOM =======================
@Composable
fun LiveAudioCallingRoom(viewModel: PrashnaViewModel) {
    val duration by viewModel.callDurationSeconds.collectAsStateWithLifecycle()
    val activeHost by viewModel.activeHost.collectAsStateWithLifecycle()
    val topic by viewModel.conversationTopic.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val isVoiceMaskingEnabled by viewModel.isVoiceMaskingEnabled.collectAsStateWithLifecycle()
    val suggestions by viewModel.socraticSuggestions.collectAsStateWithLifecycle()
    val isLoadingSuggestions by viewModel.isSuggestionsLoading.collectAsStateWithLifecycle()

    // Breathing pulse infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .testTag("calling_room"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE AUDIO SECURE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }

            Text(
                text = viewModel.formatDuration(duration),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Host Name and Topic
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = activeHost?.name ?: "Verified Philosopher",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Topic: \"$topic\"",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = activeHost?.specialties ?: "",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Breathing circle visualizer
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner glowing core
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Active call microphone",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Subtitle instructions
            Box(modifier = Modifier.offset(y = 100.dp)) {
                Text(
                    text = "Breathe with the pulse",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Real-time AI Socratic suggestions card!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("socratic_hints_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "AI Suggestions",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Real-time AI Socratic Prompts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { activeHost?.let { viewModel.fetchSocraticPrompts(it, topic) } },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh prompts",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoadingSuggestions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Socratic engine thinking...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                } else if (suggestions.isEmpty()) {
                    Text(
                        text = "Approach the dialogue with silence. As the host guides you, insights will trigger here.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    suggestions.forEachIndexed { idx, hint ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = hint,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Live Call Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .size(52.dp)
                    .background(if (isMuted) Color.Red.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
                    .border(1.5.dp, if (isMuted) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                    .testTag("mute_btn")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute button",
                    tint = if (isMuted) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }

            // End Call Button (Big round red button)
            Button(
                onClick = { viewModel.endCallAndAnalyze() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("end_call_btn")
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End dialogue", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("End Conversation", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Voice Masking Toggle
            IconButton(
                onClick = { viewModel.toggleVoiceMasking() },
                modifier = Modifier
                    .size(52.dp)
                    .background(if (isVoiceMaskingEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
                    .border(1.5.dp, if (isVoiceMaskingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                    .testTag("voice_mask_btn")
            ) {
                Icon(
                    imageVector = if (isVoiceMaskingEnabled) Icons.Default.RecordVoiceOver else Icons.Default.Portrait,
                    contentDescription = "Voice mask",
                    tint = if (isVoiceMaskingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ======================= POST CALL REFLECTION SCREEN =======================
@Composable
fun PostCallReflectionDialog(viewModel: PrashnaViewModel) {
    val isAnalyzing by viewModel.isAnalyzingSession.collectAsStateWithLifecycle()
    val analysis by viewModel.sessionAnalysisResult.collectAsStateWithLifecycle()
    val activeHost by viewModel.activeHost.collectAsStateWithLifecycle()
    val topic by viewModel.conversationTopic.collectAsStateWithLifecycle()

    val localContext = LocalContext.current

    Dialog(onDismissRequest = { /* Cannot dismiss until completed to keep focus */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("reflection_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            if (isAnalyzing) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Analyzing Conversation...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The AI Socratic Engine is transcribing, scoring curiosity and active listening quality, and formulating customized reflection exercises...",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "AI WISDOM SUMMARY",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Self-Reflection Journal",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Dialogue with ${activeHost?.name ?: "Acharya"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Metrics Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Inquiry Quality Metrics", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Curiosity Bar
                                ScoreRow(title = "Intellectual Curiosity", score = analysis?.curiosityScore ?: 85, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Listening Bar
                                ScoreRow(title = "Active Absorption / Listening", score = analysis?.listeningScore ?: 90, color = SuccessGreen)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Respect/Humility Bar
                                ScoreRow(title = "Intellectual Humility & Respect", score = analysis?.respectScore ?: 95, color = AccentGold)
                            }
                        }
                    }

                    // Deep Summary
                    item {
                        Column {
                            Text("Deep Core Summary", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            Text(
                                text = analysis?.summary ?: "",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Qualitative Socratic Guidance
                    item {
                        Column {
                            Text("Philosophical Feedback", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            Text(
                                text = analysis?.feedback ?: "",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Socratic Reflection Exercise
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = "Exercise", tint = AccentGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Actionable Reflection Exercise", fontWeight = FontWeight.Bold, color = AccentGold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = analysis?.reflectionExercise ?: "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Recommended Readings
                    item {
                        Column {
                            Text("Recommended Classical Readings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            analysis?.recommendedReadings?.forEach { reading ->
                                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.MenuBook, contentDescription = "Book", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp).offset(y = 2.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = reading, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }

                    // Bottom actions
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    analysis?.let {
                                        viewModel.saveJournalEntry(
                                            title = "Reflection on $topic",
                                            category = "Self-Inquiry",
                                            content = "${it.summary}\n\nExercise:\n${it.reflectionExercise}"
                                        )
                                        viewModel.dismissPostCall()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .testTag("integrate_journal_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save to Journal")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add to Journal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.dismissPostCall() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), contentColor = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .testTag("dismiss_reflection_btn")
                            ) {
                                Text("Dismiss", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreRow(title: String, score: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(text = "$score%", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}
