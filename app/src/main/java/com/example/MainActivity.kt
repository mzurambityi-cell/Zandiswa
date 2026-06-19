package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter
import com.example.data.ListingType
import com.example.data.Property
import com.example.data.PropertyType
import com.example.data.local.AppDatabase
import com.example.data.local.PropertyRepository
import com.example.ui.ChatMessage
import com.example.ui.FilterState
import com.example.ui.PropertyViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.lazy.itemsIndexed

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PropertyRepository(database.propertyDao())
        val viewModel = ViewModelProvider(
            this,
            PropertyViewModel.Factory(repository)
        )[PropertyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class HomeTab(val label: String, val icon: @Composable () -> Unit) {
    DISCOVER("Discover", { Icon(Icons.Default.Home, contentDescription = "Discover") }),
    SEARCH("Search", { Icon(Icons.Default.Search, contentDescription = "Search & Filter") }),
    SELL("Sell", { Icon(Icons.Default.Business, contentDescription = "Sell & Find Agent") }),
    SAVED("Saved", { Icon(Icons.Default.Favorite, contentDescription = "Saved/Liked") }),
    ASSISTANT("AI Advisor", { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "AI Assistant") })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PropertyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(HomeTab.DISCOVER) }
    val selectedProperty by viewModel.selectedProperty.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
        AppHeader(
            activeTab = currentTab,
            onAddListingClicked = {
                currentTab = HomeTab.SELL // open sell tab where adding listings actually belongs now!
            }
        )

        // Contents Section
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentTab) {
                HomeTab.DISCOVER -> DiscoverTabContent(viewModel)
                HomeTab.SEARCH -> SearchTabContent(viewModel)
                HomeTab.SELL -> SellTabContent(viewModel, onListingCreated = { currentTab = HomeTab.SEARCH })
                HomeTab.SAVED -> SavedTabContent(viewModel)
                HomeTab.ASSISTANT -> AIAssistantTabContent(viewModel)
            }
        }

        // Custom Safe Bottom Navigation Bar
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            HomeTab.values().forEach { tab ->
                val selected = currentTab == tab
                NavigationBarItem(
                    selected = selected,
                    onClick = { currentTab = tab },
                    icon = tab.icon,
                    label = {
                        Text(
                            text = tab.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                )
            }
        }
    }

    // Detail Bottom Sheet if any property is selected
    if (selectedProperty != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { viewModel.selectedProperty.value = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PropertyDetailContent(
                property = selectedProperty!!,
                viewModel = viewModel,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        viewModel.selectedProperty.value = null
                    }
                }
            )
        }
    }
}

@Composable
fun AppHeader(
    activeTab: HomeTab,
    onAddListingClicked: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Zandiswa Properties",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = when (activeTab) {
                        HomeTab.DISCOVER -> "Professional Valuations, Sales & Rentals"
                        HomeTab.SEARCH -> "Tailor your house search criteria"
                        HomeTab.SELL -> "List local properties & find certified agents"
                        HomeTab.SAVED -> "Your custom portfolio shortlist"
                        HomeTab.ASSISTANT -> "Gemini real estate advisor agent"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (activeTab == HomeTab.DISCOVER) {
                IconButton(
                    onClick = onAddListingClicked,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "List Home",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverTabContent(viewModel: PropertyViewModel) {
    val properties by viewModel.allPropertiesInApp.collectAsState()
    val savedIds by viewModel.savedPropertyIds.collectAsState()
    val filters by viewModel.filterState.collectAsState()
    val currentDisplayList = if (filters.suburb == null) properties else properties.filter { it.suburb == filters.suburb }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Image Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_wc_hero_1781794069697),
                    contentDescription = "Scenic Western Cape Table Mountain House",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text("Featured Zandiswa Listings", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Text(
                        text = "Your Dream Home Awaits",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Browse premier properties curated by Zandiswa Properties.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Zandiswa Corporate Marketing & Valuation Banner (directly from the provided marketing card)
        item {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Zandiswa Properties Logo",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp).padding(end = 6.dp)
                        )
                        Text(
                            text = "THINKING OF BUYING OR SELLING?",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Gold horizontal divider bar (from image)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "IF YOU WANT TO BUY OR SELL YOUR HOUSE.\nIF YOU WANT TO RENT OR RENT OUT YOUR PROPERTY.\nFOR A FREE VALUATION, PLEASE CONTACT US!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gold horizontal divider bar bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "Patrick Beja | Nolulufefe Beja",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📞 +27 (0)76 786 0422\n📞 +27 (0)73 627 4193",
                                fontSize = 11.sp,
                                color = Color.White,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📧 patrickbeja1966@gmail.com",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(0.9f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "📍 Main Office:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "56 Zodiac street,\nKhulani Park,\nKhayelitsha, 7784",
                                fontSize = 9.sp,
                                color = Color.White,
                                lineHeight = 12.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        // Suburbs horizontal row selector
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Browse Prominent Areas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(com.example.data.WesternCapePropertyData.suburbs) { suburb ->
                        val isSelected = filters.suburb == suburb
                        val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        val chipText = if (isSelected) Color.White else MaterialTheme.colorScheme.primary

                        Surface(
                            shape = CircleShape,
                            color = chipBg,
                            modifier = Modifier
                                .clickable {
                                    if (isSelected) {
                                        viewModel.filterState.value = filters.copy(suburb = null)
                                    } else {
                                        viewModel.filterState.value = filters.copy(suburb = suburb)
                                    }
                                }
                        ) {
                            Text(
                                text = suburb,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = chipText
                            )
                        }
                    }
                }
            }
        }

        // Listings header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Curated Listings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${properties.size} properties",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Vertical property items
        if (currentDisplayList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No properties listed in this suburb yet.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(currentDisplayList) { property ->
                PropertyCard(
                    property = property,
                    isSaved = savedIds.contains(property.id),
                    onToggleSaved = { viewModel.toggleFavorite(property.id) },
                    onSelect = { viewModel.selectedProperty.value = property }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchTabContent(viewModel: PropertyViewModel) {
    val filteredProps by viewModel.filteredProperties.collectAsState()
    val savedIds by viewModel.savedPropertyIds.collectAsState()
    val filters by viewModel.filterState.collectAsState()
    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var priceInput by remember { mutableStateOf(filters.maxPrice?.toInt()?.toString() ?: "") }
    
    LaunchedEffect(filters.maxPrice) {
        if (filters.maxPrice == null) {
            priceInput = ""
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Multi Criteria Filter Panel Card
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Advanced Search Filters", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        // Add property button
                        Button(
                            onClick = { showAddPropertyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_property_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Listing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Text Search Input
                    OutlinedTextField(
                        value = filters.searchQuery,
                        onValueChange = { viewModel.filterState.value = filters.copy(searchQuery = it) },
                        placeholder = { Text("Search title, suburb or keyword...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Listing Type Tabs (Buy vs Rent vs All)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(null, ListingType.BUY, ListingType.RENT).forEach { type ->
                            val label = type?.displayName ?: "All Listings"
                            val isSelected = filters.listingType == type
                            OutlinedButton(
                                onClick = { viewModel.filterState.value = filters.copy(listingType = type) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Property Type Flow Row selection
                    Text("Property Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PropertyType.values().forEach { pType ->
                            val isSelected = filters.propertyType == pType
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.filterState.value = filters.copy(
                                            propertyType = if (isSelected) null else pType
                                        )
                                    }
                            ) {
                                Text(
                                    text = pType.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Price & Bedroom Quick Select
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Max Price field
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = {
                                priceInput = it
                                val parsed = it.toDoubleOrNull()
                                viewModel.filterState.value = filters.copy(maxPrice = parsed)
                            },
                            label = { Text("Max Budget (ZAR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Min Bedrooms dropdown style
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable {
                                        val nextBeds = if (filters.minBedrooms >= 4) 0 else filters.minBedrooms + 1
                                        viewModel.filterState.value = filters.copy(minBedrooms = nextBeds)
                                    }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Min Beds", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text(
                                        text = if (filters.minBedrooms > 0) "${filters.minBedrooms}+ Beds" else "Any Beds",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(Icons.Default.Bed, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            viewModel.clearFilters()
                            priceInput = ""
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reset All Filters", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search Results List
        item {
            Text(
                text = "Matching Search Results (${filteredProps.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (filteredProps.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No properties match your filters.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredProps) { prop ->
                PropertyCard(
                    property = prop,
                    isSaved = savedIds.contains(prop.id),
                    onToggleSaved = { viewModel.toggleFavorite(prop.id) },
                    onSelect = { viewModel.selectedProperty.value = prop }
                )
            }
        }
    }

    // Add Simulated Property Dialog Modal
    if (showAddPropertyDialog) {
        AddSimulatedPropertyDialog(
            onDismiss = { showAddPropertyDialog = false },
            onConfirm = { title, price, suburb, type, listType, beds, baths, size, desc, broker, tel, email ->
                viewModel.addCustomProperty(
                    title, price, suburb, type, listType, beds, baths, size, desc, broker, tel, email
                )
                showAddPropertyDialog = false
            }
        )
    }
}

@Composable
fun SavedTabContent(viewModel: PropertyViewModel) {
    val properties by viewModel.allPropertiesInApp.collectAsState()
    val savedIds by viewModel.savedPropertyIds.collectAsState()
    val bookmarkedList = remember(properties, savedIds) {
        properties.filter { savedIds.contains(it.id) }
    }

    if (bookmarkedList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Shortlist is Empty",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Keep track of dream properties you love by tapping the heart icon on cards.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Offline Saved Houses (${bookmarkedList.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )
            }
            items(bookmarkedList) { prop ->
                PropertyCard(
                    property = prop,
                    isSaved = true,
                    onToggleSaved = { viewModel.toggleFavorite(prop.id) },
                    onSelect = { viewModel.selectedProperty.value = prop }
                )
            }
        }
    }
}

@Composable
fun AIAssistantTabContent(viewModel: PropertyViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var typedText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick suggestions row
        val suggestionPrompts = listOf(
            "Which areas are cheapest?",
            "Any beach villas in Camps Bay?",
            "Stellenbosch wine estate cottages?",
            "Renting vs buying transfer duties?"
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            items(suggestionPrompts) { text ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(text)
                    }
                ) {
                    Text(
                        text = text,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Chat messages bubble log
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini Realtor thinking...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Input bottom bar
        Surface(
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    placeholder = { Text("Ask about rentals, suburbs, solar standard...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_query_input"),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (typedText.isNotBlank()) {
                            viewModel.sendChatMessage(typedText)
                            typedText = ""
                            keyboardController?.hide()
                        }
                    }),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (typedText.isNotBlank()) {
                            viewModel.sendChatMessage(typedText)
                            typedText = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        Text(
            text = if (message.isUser) "You" else "Gemini Advisor",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp)
        )
    }
}

@Composable
fun PropertyCard(
    property: Property,
    isSaved: Boolean,
    onToggleSaved: () -> Unit,
    onSelect: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZA")) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onSelect)
            .testTag("property_card_${property.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Async property photo
                Image(
                    painter = rememberAsyncImagePainter(model = property.imageUrl),
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Listing type luxury tag (Buy / Rent)
                Badge(
                    containerColor = if (property.listingType == ListingType.BUY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "For ${property.listingType.displayName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Suburb area watermark tag
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = property.suburb,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Bookmark toggle heart
                IconButton(
                    onClick = onToggleSaved,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save house",
                        tint = if (isSaved) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Info Details
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = property.type.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)).padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Text(
                        text = if (property.listingType == ListingType.RENT) {
                            "${currencyFormatter.format(property.price)} / mo"
                        } else {
                            currencyFormatter.format(property.price)
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = property.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bed, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${property.bedrooms} Beds", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bathtub, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${property.bathrooms} Baths", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SquareFoot, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${property.sizeSqm} sqm", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyDetailContent(
    property: Property,
    viewModel: PropertyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val savedIds by viewModel.savedPropertyIds.collectAsState()
    val isSaved = savedIds.contains(property.id)
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZA")) }

    // Dialog state for viewing schedule
    var showBookingDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = property.imageUrl),
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient header cover
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(property.id) },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Save house",
                                tint = if (isSaved) Color.Red else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Details fields
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            text = "${property.type.displayName} • For ${property.listingType.displayName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = if (property.listingType == ListingType.RENT) {
                            "${currencyFormatter.format(property.price)} / month"
                        } else {
                            currencyFormatter.format(property.price)
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = property.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${property.suburb}, ${property.city}, South Africa",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amenities badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Bed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("${property.bedrooms} Bedrooms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Bathtub, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("${property.bathrooms} Bathrooms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SquareFoot, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("${property.sizeSqm} Sqm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Description",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = property.description,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Back power inverter / water bonus badges
                Text(
                    text = "Key Infrastructure Standard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Full Inverter Installed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Secure Gated Estate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Agent representation card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Agent Initial avatar
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = property.agentName.substring(0, 1),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = property.agentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Zandiswa Properties Certified Consultant",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interaction channels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Simulating secure call to agent ${property.agentName} at ${property.agentPhone}", Toast.LENGTH_LONG).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Simulating email to ${property.agentEmail} regarding: ${property.title}", Toast.LENGTH_LONG).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showBookingDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Book Viewing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBookingDialog) {
        BookingViewingDialog(
            propertyName = property.title,
            agentName = property.agentName,
            onDismiss = { showBookingDialog = false },
            onConfirm = { date, time, msg ->
                Toast.makeText(context, "Viewing Scheduled! Zanele/Stefan will confirm: $date at $time.", Toast.LENGTH_LONG).show()
                showBookingDialog = false
            }
        )
    }
}

@Composable
fun BookingViewingDialog(
    propertyName: String,
    agentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var msgInput by remember { mutableStateOf("Hi $agentName, I would like to schedule a viewing for this beautiful property.") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Request Viewing Time",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = propertyName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Date (e.g., June 22, 2026)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text("Time Slot (e.g., 14:00)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = msgInput,
                    onValueChange = { msgInput = it },
                    label = { Text("Note to agent") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (dateInput.isNotBlank() && timeInput.isNotBlank()) {
                                onConfirm(dateInput, timeInput, msgInput)
                            } else {
                                onConfirm("Tomorrow", "11:00 AM", msgInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Send Proposal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddSimulatedPropertyDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        price: Double,
        suburb: String,
        type: PropertyType,
        listingType: ListingType,
        bedrooms: Int,
        bathrooms: Int,
        sizeSqm: Int,
        description: String,
        agentName: String,
        agentPhone: String,
        agentEmail: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedSuburb by remember { mutableStateOf("Cape Town") }
    var type by remember { mutableStateOf(PropertyType.HOUSE) }
    var listingType by remember { mutableStateOf(ListingType.BUY) }
    var bedsStr by remember { mutableStateOf("3") }
    var bathsStr by remember { mutableStateOf("2") }
    var sizeStr by remember { mutableStateOf("180") }
    var description by remember { mutableStateOf("") }

    val suburbs = com.example.data.WesternCapePropertyData.suburbs

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                item {
                    Text(
                        text = "List Your Property with Zandiswa",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your property will instantly appear in search results.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Listing Title") },
                        modifier = Modifier.fillMaxWidth().testTag("add_title_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Price (ZAR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("add_price_field"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Listing Method", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable {
                                        listingType = if (listingType == ListingType.BUY) ListingType.RENT else ListingType.BUY
                                    }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(listingType.displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Column {
                        Text("Select Property Type", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            PropertyType.values().forEach { pType ->
                                val active = type == pType
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { type = pType }
                                ) {
                                    Text(
                                        pType.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Column {
                        Text("Select Suburb Area", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(suburbs) { sub ->
                                val active = selectedSuburb == sub
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.clickable { selectedSuburb = sub }
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bedsStr,
                            onValueChange = { bedsStr = it },
                            label = { Text("Beds") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = bathsStr,
                            onValueChange = { bathsStr = it },
                            label = { Text("Baths") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = sizeStr,
                            onValueChange = { sizeStr = it },
                            label = { Text("Size (sqm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Property Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && priceStr.isNotBlank()) {
                                    onConfirm(
                                        title,
                                        priceStr.toDoubleOrNull() ?: 5000000.0,
                                        selectedSuburb,
                                        type,
                                        listingType,
                                        bedsStr.toIntOrNull() ?: 3,
                                        bathsStr.toIntOrNull() ?: 2,
                                        sizeStr.toIntOrNull() ?: 180,
                                        if (description.isBlank()) "Beautiful custom listed house in Western Cape." else description,
                                        "User Agent",
                                        "+27 (00) 000-0000",
                                        "user_listed@westerncapehomes.co.za"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Publish Listing", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class SellAgent(
    val id: String,
    val name: String,
    val title: String,
    val phone: String,
    val email: String,
    val imageUrl: String,
    val rating: Double,
    val reviews: Int,
    val specialties: String,
    val bio: String,
    val badge: String
)

val sellAgents = listOf(
    SellAgent(
        id = "nolulufefe",
        name = "Nolulufefe Beja",
        title = "Co-Founder & Managing Director",
        phone = "+27 (0)73 627 4193",
        email = "patrickbeja1966@gmail.com",
        imageUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=300&q=80",
        rating = 5.0,
        reviews = 142,
        specialties = "Khayelitsha, Camps Bay, Sea Point",
        bio = "Nolulufefe is the Co-Founder of Zandiswa Properties. She is dedicated to community enrichment, professional property valuations, and providing premium real estate advisory.",
        badge = "Founding Director"
    ),
    SellAgent(
        id = "patrick",
        name = "Patrick Beja",
        title = "Co-Founder & Executive Director",
        phone = "+27 (0)76 786 0422",
        email = "patrickbeja1966@gmail.com",
        imageUrl = "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=300&q=80",
        rating = 5.0,
        reviews = 158,
        specialties = "Khayelitsha, Mitchells Plain, Somerset West, Stellenbosch",
        bio = "Patrick is the Co-Founder and Director of Zandiswa Properties. With over 15+ years of experience, he leads property acquisitions, sales, and landlord negotiations.",
        badge = "Founding Director"
    ),
    SellAgent(
        id = "zanele",
        name = "Zanele Nkosi",
        title = "Principal Atlantic Seaboard Consultant",
        phone = "+27 (82) 555-0192",
        email = "zanele@zandiswaproperties.co.za",
        imageUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80",
        rating = 4.9,
        reviews = 94,
        specialties = "Camps Bay, Constantia, Sea Point",
        bio = "Zanele is our lead partner specializing in luxury rentals and multi-million rand sea-view estates.",
        badge = "Prestige Gold Circle"
    ),
    SellAgent(
        id = "stefan",
        name = "Stefan van der Merwe",
        title = "Cape Winelands Partner Specialist",
        phone = "+27 (83) 555-2345",
        email = "stefan@zandiswaproperties.co.za",
        imageUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=300&q=80",
        rating = 4.8,
        reviews = 82,
        specialties = "Stellenbosch, Franschhoek",
        bio = "Stefan brings deep architectural knowledge of Cape Winelands estates, security estates, and country living.",
        badge = "Winelands Director"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellTabContent(
    viewModel: PropertyViewModel,
    onListingCreated: () -> Unit
) {
    var subTabState by remember { mutableStateOf(0) } // 0 = Sell Form, 1 = Agent Finder
    
    // Shared state so selecting an agent highlights/binds them as the designated listing coordinator
    var selectedAgent by remember { mutableStateOf(sellAgents[0]) }
    
    // Sell Form State
    var title by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedSuburb by remember { mutableStateOf("Camps Bay") }
    var propertyType by remember { mutableStateOf(PropertyType.HOUSE) }
    var listingType by remember { mutableStateOf(ListingType.BUY) }
    var bedsStr by remember { mutableStateOf("3") }
    var bathsStr by remember { mutableStateOf("2") }
    var sizeStr by remember { mutableStateOf("180") }
    var description by remember { mutableStateOf("") }
    
    // Picture loading states (Starts with 1 default loaded facade)
    val loadedPictures = remember { mutableStateListOf<String>(
        "https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=800&q=80"
    ) }
    var customUrlInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Sliding tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val isForm = subTabState == 0
            val isAgents = subTabState == 1
            
            Surface(
                onClick = { subTabState = 0 },
                color = if (isForm) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isForm) Color.White else MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("sell_tab_form_toggle")
            ) {
                Text(
                    text = "List Property to Sell",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
            
            Surface(
                onClick = { subTabState = 1 },
                color = if (isAgents) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isAgents) Color.White else MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("sell_tab_agents_toggle")
            ) {
                Text(
                    text = "Find Certified Agent",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
        
        if (subTabState == 0) {
            // Sell Form
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Text(
                        text = "1. Property Overview Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                
                // Form Fields
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Property Title") },
                        placeholder = { Text("e.g. Camps Bay Modern Sunset Villa") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sell_input_title"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Target Price (ZAR R)") },
                            placeholder = { Text("e.g. 8500000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("sell_input_price"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        
                        Column(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    listingType = if (listingType == ListingType.BUY) ListingType.RENT else ListingType.BUY
                                }
                                .padding(horizontal = 10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Category", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(listingType.displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                
                item {
                    Column {
                        Text("Property Type", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            PropertyType.values().forEach { pType ->
                                val active = propertyType == pType
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { propertyType = pType }
                                ) {
                                    Text(
                                        text = pType.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                
                item {
                    Column {
                        Text("Suburb Area", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(com.example.data.WesternCapePropertyData.suburbs) { sub ->
                                val active = selectedSuburb == sub
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.clickable { selectedSuburb = sub }
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bedsStr,
                            onValueChange = { bedsStr = it },
                            label = { Text("Bedrooms") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = bathsStr,
                            onValueChange = { bathsStr = it },
                            label = { Text("Bathrooms") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = sizeStr,
                            onValueChange = { sizeStr = it },
                            label = { Text("Size (sqm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Full Property Description") },
                        placeholder = { Text("Describe views, loadshedding backups, special security features in detail...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3,
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Photo loading Section!
                item {
                    Text(
                        text = "2. Load & Prepare Photos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Real estate portfolios with loaded pictures get 4x higher bookings.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Pictures loaded deck representation
                    if (loadedPictures.isNotEmpty()) {
                        Text(
                            text = "CURRENTLY LOADED (${loadedPictures.size}) (Tap trash to remove)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            itemsIndexed(loadedPictures) { index, itemUrl ->
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = itemUrl),
                                        contentDescription = "Property photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    
                                    // Featured visual overlay on the first picture
                                    if (index == 0) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(bottomEnd = 6.dp),
                                            modifier = Modifier.align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                "COVER", 
                                                fontSize = 8.sp, 
                                                fontWeight = FontWeight.Bold, 
                                                color = Color.White, 
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    
                                    // Floating mini delete trash target
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(22.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .clickable { loadedPictures.removeAt(index) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Delete, 
                                            contentDescription = "Remove photo", 
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        // Empty states warning
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Please load at least one photo to publish! Check options below.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    
                    // Quick add gallery presets
                    Text(
                        text = "ADD HIGH-RESOLUTION REAL ESTATE PRESETS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    val presets = listOf(
                        Pair("https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=300&q=80", "Front Facade"),
                        Pair("https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=300&q=80", "Modern Kitchen"),
                        Pair("https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=300&q=80", "Master Bed"),
                        Pair("https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=300&q=80", "Sunlight Patio"),
                        Pair("https://images.unsplash.com/photo-1507089947368-19c1da9775ae?auto=format&fit=crop&w=300&q=80", "Infinity Pool")
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { preset ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(75.dp)
                                    .clickable {
                                        if (!loadedPictures.contains(preset.first)) {
                                            loadedPictures.add(preset.first)
                                            Toast.makeText(context, "${preset.second} loaded!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = preset.first),
                                        contentDescription = preset.second,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text(preset.second, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Paste Custom Photo URL & Camera simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customUrlInput,
                            onValueChange = { customUrlInput = it },
                            label = { Text("Paste Custom Photo URL") },
                            placeholder = { Text("https://example.com/photo.jpg") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                if (customUrlInput.isNotBlank()) {
                                    loadedPictures.add(customUrlInput.trim())
                                    customUrlInput = ""
                                    Toast.makeText(context, "URL photo loaded successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Load")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Simulated Camera Capture trigger
                    OutlinedButton(
                        onClick = {
                            val simulatedPic = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=400&q=80"
                            loadedPictures.add(simulatedPic)
                            Toast.makeText(context, "[Simulator] Captured live property photo with smartphone!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Snapshot Photo Capture", fontWeight = FontWeight.SemiBold)
                    }
                }
                
                // Assigned Agent coordinator representation
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "3. Assigned Real Estate Coordinator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Your listing is assigned to a certified local consultant who will verify and list it.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = selectedAgent.imageUrl),
                                    contentDescription = selectedAgent.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedAgent.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(selectedAgent.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("Focus areas: ${selectedAgent.specialties}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            // Change action
                            TextButton(onClick = { subTabState = 1 }) {
                                Text("Change", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                // Submit Listing Block
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Please enter a Property Title!", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if (priceStr.isBlank() || priceStr.toDoubleOrNull() == null) {
                                Toast.makeText(context, "Please enter a valid Target Price!", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if (loadedPictures.isEmpty()) {
                                Toast.makeText(context, "Please load at least one photo!", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            
                            // Add property with custom image!
                            viewModel.addCustomProperty(
                                title = title,
                                price = priceStr.toDoubleOrNull() ?: 3500000.0,
                                suburb = selectedSuburb,
                                type = propertyType,
                                listingType = listingType,
                                bedrooms = bedsStr.toIntOrNull() ?: 3,
                                bathrooms = bathsStr.toIntOrNull() ?: 2,
                                sizeSqm = sizeStr.toIntOrNull() ?: 180,
                                description = if (description.isBlank()) "Spacious contemporary architectural residence in heart of ${selectedSuburb}. Loaded features and backup grid installed." else description,
                                agentName = selectedAgent.name,
                                agentPhone = selectedAgent.phone,
                                agentEmail = selectedAgent.email,
                                imageUrl = loadedPictures.firstOrNull() // Pass our first loaded picture!
                            )
                            
                            Toast.makeText(context, "Success! '${title}' has been listed by ${selectedAgent.name}!", Toast.LENGTH_LONG).show()
                            onListingCreated() // Redirect to the Search page
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("sell_publish_button")
                    ) {
                        Text("Confirm & Publish with My Agent", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        } else {
            // Agent Directory Screen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Zandiswa Properties Executive Directors & Agents",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Partner with leading agency consultants to securely list or browse listings.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                items(sellAgents) { agent ->
                    val isAssigned = selectedAgent.id == agent.id
                    
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("agent_card_${agent.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isAssigned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Image, Name, Badge
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isAssigned) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = agent.imageUrl),
                                        contentDescription = agent.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            agent.name, 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("${agent.rating} (${agent.reviews})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(agent.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    
                                    // Professional Badge
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = agent.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Bio
                            Text(agent.bio, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Specialty Badges
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Specialties: ${agent.specialties}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Interactive actions panel
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Contact Shortcuts: Call and Email
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Routing call to ${agent.name} at ${agent.phone}...", Toast.LENGTH_SHORT).show()
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${agent.phone}"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.2f).height(38.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call Agent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Preparing email to ${agent.email}...", Toast.LENGTH_SHORT).show()
                                        try {
                                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${agent.email}")).apply {
                                                putExtra(Intent.EXTRA_SUBJECT, "Inquiry on Western Cape property listing partnership")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.2f).height(38.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                // Assignment Button connects the Find Agent workflow back to the listings flow!
                                Button(
                                    onClick = {
                                        selectedAgent = agent
                                        subTabState = 0 // Switch back to form instantly
                                        Toast.makeText(context, "Coordinator partner changed to ${agent.name}! Check form below.", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAssigned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.8f).height(38.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isAssigned) "Assigned" else "Assign Partner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
