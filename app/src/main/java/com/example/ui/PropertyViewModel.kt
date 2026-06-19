package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.ListingType
import com.example.data.Property
import com.example.data.PropertyType
import com.example.data.WesternCapePropertyData
import com.example.data.gemini.Content
import com.example.data.gemini.GenerateContentRequest
import com.example.data.gemini.GenerationConfig
import com.example.data.gemini.Part
import com.example.data.gemini.RetrofitClient
import com.example.data.local.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class FilterState(
    val searchQuery: String = "",
    val listingType: ListingType? = null,
    val suburb: String? = null,
    val propertyType: PropertyType? = null,
    val maxPrice: Double? = null,
    val minBedrooms: Int = 0
)

class PropertyViewModel(private val repository: PropertyRepository) : ViewModel() {

    // Dynamic property list starting with our preset South Africa data
    private val _allPropertiesInApp = MutableStateFlow<List<Property>>(WesternCapePropertyData.properties)
    val allPropertiesInApp: StateFlow<List<Property>> = _allPropertiesInApp

    // Filters UI State
    val filterState = MutableStateFlow(FilterState())

    // Saved property IDs from Room Database
    val savedPropertyIds: StateFlow<List<Int>> = repository.savedPropertyIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combined filtered properties state
    val filteredProperties: StateFlow<List<Property>> = combine(
        _allPropertiesInApp,
        filterState
    ) { properties, filters ->
        properties.filter { p ->
            val matchQuery = filters.searchQuery.isEmpty() ||
                    p.title.contains(filters.searchQuery, ignoreCase = true) ||
                    p.description.contains(filters.searchQuery, ignoreCase = true) ||
                    p.suburb.contains(filters.searchQuery, ignoreCase = true)

            val matchListingType = filters.listingType == null || p.listingType == filters.listingType
            val matchSuburb = filters.suburb == null || p.suburb.equals(filters.suburb, ignoreCase = true)
            val matchType = filters.propertyType == null || p.type == filters.propertyType
            val matchPrice = filters.maxPrice == null || p.price <= filters.maxPrice
            val matchBed = p.bedrooms >= filters.minBedrooms

            matchQuery && matchListingType && matchSuburb && matchType && matchPrice && matchBed
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Detail Modal / Sheet state
    val selectedProperty = MutableStateFlow<Property?>(null)

    // Gemini Chat state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Molo! Welcome to Zandiswa Properties! We are a leading corporate agency founded by Patrick Beja and Nolulufefe Beja. Ask me anything about renting, buying, selling or property valuations in suburbs across the Western Cape. How can I assist you on our corporate portal today?",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    // Toggle Bookmarks/Favorites in SQLite Room Database
    fun toggleFavorite(propertyId: Int) {
        viewModelScope.launch {
            val ids = savedPropertyIds.value
            if (ids.contains(propertyId)) {
                repository.removeProperty(propertyId)
            } else {
                repository.saveProperty(propertyId)
            }
        }
    }

    // Add custom simulated property listings
    fun addCustomProperty(
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
        agentEmail: String,
        imageUrl: String? = null
    ) {
        val newId = (_allPropertiesInApp.value.maxOfOrNull { it.id } ?: 0) + 1
        // Generate a random real estate picture from Unsplash for visual richness
        val defaultUrls = listOf(
            "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80"
        )
        val selectedUrl = imageUrl ?: defaultUrls[newId % defaultUrls.size]

        val newProp = Property(
            id = newId,
            title = title,
            price = price,
            type = type,
            listingType = listingType,
            suburb = suburb,
            bedrooms = bedrooms,
            bathrooms = bathrooms,
            sizeSqm = sizeSqm,
            description = description,
            agentName = agentName,
            agentPhone = agentPhone,
            agentEmail = agentEmail,
            imageUrl = selectedUrl
        )

        _allPropertiesInApp.value = _allPropertiesInApp.value + newProp
    }

    // Reset filters
    fun clearFilters() {
        filterState.value = FilterState()
    }

    // Send chat message to Gemini
    fun sendChatMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // 1. Add user message
        val userMsgRecord = ChatMessage(text = userMessage, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsgRecord
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                // Compile present properties context
                val propertiesContext = _allPropertiesInApp.value.joinToString("\n") { p ->
                    "- ${p.title} in ${p.suburb} (ZAR R${p.price}, ${p.type.displayName}, for ${p.listingType.displayName}, ${p.bedrooms} beds, ${p.bathrooms} baths)"
                }

                val systemPrompt = """
                    You are a highly experienced, warm, and professional local real estate advisor for Zandiswa Properties, South Africa, a premier agency co-founded by Patrick Beja and Nolulufefe Beja.
                    Your goal is to guide users who are searching to rent, buy, sell, or get valuations for homes.
                    
                    Be very knowledgeable about areas in Western Cape:
                    - Camps Bay & Sea Point: coastal luxury, high prices, Atlantic ocean sunsets.
                    - Khayelitsha: vibrant township area, growing residential and local commercial space.
                    - Stellenbosch & Franschhoek: gorgeous winelands, oak-shaded streets, historical cottages, mountain vistas.
                    - Hermanus: peaceful seaside paradise, whale watching.
                    - Knysna: tranquil lakes, lagoon, epic Garden Route nature.
                    - Muizenberg: beach town, surfer's paradise, vibrant and active.
                    - Constantia: leafy, upscale greenbelts, classic country mansions.
                    
                    Here are the properties listed on the app:
                    $propertiesContext
                    
                    When users ask for property suggestions, match their specific description against these properties.
                    Recommend matches beautifully, including prices, locations, and bed counts. Mention that Patrick Beja and Nolulufefe Beja are available to render professional consultations and free valuations.
                    Always sign off with a cheerful, localized "Zandiswa Properties Advisor" vibe. Keep answers clear, beautifully bulleted, and informative.
                """.trimIndent()

                // Compile conversation history
                val historyParts = _chatMessages.value.takeLast(10).map { msg ->
                    Part(text = "${if (msg.isUser) "User" else "Consultant"}: ${msg.text}")
                }
                
                val combinedPromptText = systemPrompt + "\n\nConversation so far:\n" + historyParts.joinToString("\n") + "\nConsultant:"

                val apiKey = BuildConfig.GEMINI_API_KEY
                
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        text = "I'm running in offline mode because the GEMINI_API_KEY is not configured yet in your AI Studio secrets! Here is a friendly response:\n\nIf you are searching, I highly suggest Stellenbosch or Camps Bay. You can bookmark listings offline or view agent details on our screens!",
                        isUser = false
                    )
                    _isChatLoading.value = false
                    return@launch
                }

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = combinedPromptText)))),
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Standard advisor response: I'm currently consulting on multiple offers. Please write again or feel free to check out our detail screens to call the agents directly."

                _chatMessages.value = _chatMessages.value + ChatMessage(text = replyText, isUser = false)

            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    text = "Apologies, my network wave broke! Let me recommend exploring our direct listing tabs above, or contact our featured local agents: Zanele, Stefan, and Sarah.",
                    isUser = false
                )
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    class Factory(private val repository: PropertyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PropertyViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
