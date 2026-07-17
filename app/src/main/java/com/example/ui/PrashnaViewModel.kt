package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PrashnaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = PrashnaRepository(database)

    // UI exposed flows from DB
    val hosts: StateFlow<List<Host>> = repository.allHosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntries: StateFlow<List<JournalEntry>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<Session>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<UserBadge>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated Credits
    private val _walletBalance = MutableStateFlow(120.00) // Start with 120 credits
    val walletBalance = _walletBalance.asStateFlow()

    // Active Live Session State
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive = _isCallActive.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds = _callDurationSeconds.asStateFlow()

    private val _activeHost = MutableStateFlow<Host?>(null)
    val activeHost = _activeHost.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _isVoiceMaskingEnabled = MutableStateFlow(false)
    val isVoiceMaskingEnabled = _isVoiceMaskingEnabled.asStateFlow()

    private val _conversationTopic = MutableStateFlow("The Nature of Absolute Reality")
    val conversationTopic = _conversationTopic.asStateFlow()

    private val _socraticSuggestions = MutableStateFlow<List<String>>(emptyList())
    val socraticSuggestions = _socraticSuggestions.asStateFlow()

    private val _isSuggestionsLoading = MutableStateFlow(false)
    val isSuggestionsLoading = _isSuggestionsLoading.asStateFlow()

    // Post-Call Reflection Result Screen
    private val _isPostCallAnalysisActive = MutableStateFlow(false)
    val isPostCallAnalysisActive = _isPostCallAnalysisActive.asStateFlow()

    private val _isAnalyzingSession = MutableStateFlow(false)
    val isAnalyzingSession = _isAnalyzingSession.asStateFlow()

    private val _sessionAnalysisResult = MutableStateFlow<SessionAnalysis?>(null)
    val sessionAnalysisResult = _sessionAnalysisResult.asStateFlow()

    // Candidate Qualification Exam State
    private val _examQuestionIndex = MutableStateFlow(0)
    val examQuestionIndex = _examQuestionIndex.asStateFlow()

    private val _examAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // index -> selectedOptionIndex
    val examAnswers = _examAnswers.asStateFlow()

    private val _examCompleted = MutableStateFlow(false)
    val examCompleted = _examCompleted.asStateFlow()

    private val _examPassed = MutableStateFlow(false)
    val examPassed = _examPassed.asStateFlow()

    private val _showExamResult = MutableStateFlow(false)
    val showExamResult = _showExamResult.asStateFlow()

    // Timer Job for per-minute billing and duration tracking
    private var callTimerJob: Job? = null

    // Prepopulate DB on start
    init {
        viewModelScope.launch {
            repository.prePopulateIfEmpty()
        }
    }

    fun addCredits(amount: Double) {
        _walletBalance.value += amount
    }

    /**
     * Start anonymous audio call session
     */
    fun startCall(host: Host, topic: String) {
        _activeHost.value = host
        _conversationTopic.value = topic
        _callDurationSeconds.value = 0
        _isCallActive.value = true
        _isMuted.value = false
        _isVoiceMaskingEnabled.value = false
        _sessionAnalysisResult.value = null
        _isPostCallAnalysisActive.value = false
        
        // Fetch Socratic hints from Gemini API in parallel!
        fetchSocraticPrompts(host, topic)

        // Launch a coroutine to track call time and deduct credits per minute
        callTimerJob = viewModelScope.launch {
            while (_isCallActive.value) {
                delay(1000)
                _callDurationSeconds.value += 1
                
                // Every minute, deduct price of the host
                if (_callDurationSeconds.value % 60 == 0) {
                    val deduction = host.pricingPerMinute
                    if (_walletBalance.value >= deduction) {
                        _walletBalance.value -= deduction
                    } else {
                        // Out of credits, terminate call gracefully!
                        _walletBalance.value = 0.0
                        endCallAndAnalyze()
                    }
                }
            }
        }
    }

    /**
     * Terminate the session and launch the AI reflection analysis screen
     */
    fun endCallAndAnalyze() {
        val host = _activeHost.value
        val topic = _conversationTopic.value
        val durationSecs = _callDurationSeconds.value
        val durationMins = maxOf(1, (durationSecs + 59) / 60) // ceiling minutes
        val totalCost = (host?.pricingPerMinute ?: 0.0) * durationMins

        // Turn off call status
        _isCallActive.value = false
        callTimerJob?.cancel()

        if (host == null) return

        // Launch AI analytics
        _isPostCallAnalysisActive.value = true
        _isAnalyzingSession.value = true

        viewModelScope.launch {
            // Simulate deep thinking visualizer briefly or call API
            val analysis = GeminiService.analyzeSession(host.name, host.specialties, topic, durationMins)
            _sessionAnalysisResult.value = analysis
            _isAnalyzingSession.value = false

            // Save completed session record to database
            val sessionRecord = Session(
                hostName = host.name,
                durationSeconds = durationSecs,
                cost = totalCost,
                summary = analysis.summary,
                listeningScore = analysis.listeningScore,
                curiosityScore = analysis.curiosityScore,
                respectScore = analysis.respectScore
            )
            repository.insertSession(sessionRecord)

            // Unlock dynamic badges based on scores!
            unlockDeservedBadges(analysis)
        }
    }

    private suspend fun unlockDeservedBadges(analysis: SessionAnalysis) {
        // Unlock Curiosity Badge
        if (analysis.curiosityScore >= 95 && !repository.hasBadge("Sovereign Curiosity")) {
            repository.insertBadge(
                UserBadge(
                    name = "Sovereign Curiosity",
                    icon = "explore",
                    description = "Awarded for posing questions of immense philosophical depth and intellectual hunger."
                )
            )
        }
        // Unlock Listening Badge
        if (analysis.listeningScore >= 92 && !repository.hasBadge("Silent Reflection")) {
            repository.insertBadge(
                UserBadge(
                    name = "Silent Reflection",
                    icon = "hearing",
                    description = "Awarded for exceptional cognitive absorption and profound active listening."
                )
            )
        }
        // Unlock Consistency/First Dialogue Badge
        if (!repository.hasBadge("Vichar Prarambha")) {
            repository.insertBadge(
                UserBadge(
                    name = "Vichar Prarambha",
                    icon = "psychology",
                    description = "Completed your first live philosophical anonymous dialogue on Prashna."
                )
            )
        }
    }

    /**
     * Fetch progressive Socratic suggestions from Gemini
     */
    fun fetchSocraticPrompts(host: Host, topic: String) {
        _isSuggestionsLoading.value = true
        viewModelScope.launch {
            val suggestions = GeminiService.generateSocraticQuestions(host.name, host.specialties, topic)
            _socraticSuggestions.value = suggestions
            _isSuggestionsLoading.value = false
        }
    }

    /**
     * Mute action toggle
     */
    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    /**
     * Toggle voice masking (pitch alteration)
     */
    fun toggleVoiceMasking() {
        _isVoiceMaskingEnabled.value = !_isVoiceMaskingEnabled.value
    }

    /**
     * Save dynamic insight to personal learning journal
     */
    fun saveJournalEntry(title: String, category: String, content: String) {
        viewModelScope.launch {
            val entry = JournalEntry(
                title = title.ifBlank { "Silent Reflection" },
                category = category,
                content = content
            )
            repository.insertJournalEntry(entry)
            
            // Unlock Jigyasa badge for journaling!
            if (!repository.hasBadge("Vichar Manthan")) {
                repository.insertBadge(
                    UserBadge(
                        name = "Vichar Manthan",
                        icon = "edit_note",
                        description = "Awarded for committing philosophical inquiries and deep personal insights to writing."
                    )
                )
            }
        }
    }

    /**
     * Delete journal entry
     */
    fun deleteJournal(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
        }
    }

    // --- Host Examination System ---
    val examQuestions = listOf(
        ExamQuestion(
            question = "What is the primary spiritual goal of 'Karma Yoga' as presented in the Bhagavad Gita?",
            options = listOf(
                "Attaining psychic powers through strict breathing and concentration.",
                "Performing actions selflessly, dedicating fruits to the Divine, avoiding ego-attachment.",
                "Renouncing all active duties and taking vows of total silent isolation.",
                "Accumulating celestial credits (punya) for a wealthier rebirth."
            ),
            correctIndex = 1,
            topic = "Bhagavad Gita"
        ),
        ExamQuestion(
            question = "In the Mandukya Upanishad, what represents 'Turiya', the fourth state?",
            options = listOf(
                "The active dreamless sleep characterized by complete ignorance.",
                "The sensory waking consciousness focused outward on material objects.",
                "Pure, non-dual consciousness that witnesses and underlies waking, dreaming, and deep sleep.",
                "The psychological condition of deep emotional excitement during ritual chanting."
            ),
            correctIndex = 2,
            topic = "Principal Upanishads"
        ),
        ExamQuestion(
            question = "What does the Socratic method of Elenchus primarily strive to accomplish in dialogue?",
            options = listOf(
                "Entertaining onlookers with clever rhetorical triumphs and verbal gymnastics.",
                "Proving the interlocutor's lack of formal academic training.",
                "Shedding false certitudes by exposing logical inconsistencies within the student's core assumptions.",
                "Providing pre-packaged, ready-made absolute answers to complex socio-political problems."
            ),
            correctIndex = 2,
            topic = "Socratic Questioning"
        ),
        ExamQuestion(
            question = "In Nyaya (Classical Indian Logic), how is 'Anumana' (Inference) structured?",
            options = listOf(
                "Accepting verbal testimony from certified spiritual books unquestioningly.",
                "Logical inference derived from a sign (hetu) which invariably indicates a target property (sadhya).",
                "Spontaneous psychic premonition without any observational evidence.",
                "Direct sensory contact with physical items."
            ),
            correctIndex = 1,
            topic = "Logic (Nyaya)"
        ),
        ExamQuestion(
            question = "What is the core meaning of 'Anekantavada' in Jain Philosophy?",
            options = listOf(
                "The doctrine that physical world does not exist and is entirely mental hallucination.",
                "The principle of absolute non-violence in food, action, and speech.",
                "The multi-sidedness of truth; the assertion that reality is complex and cannot be fully expressed in a single viewpoint.",
                "The ritual worship of ancestors to clear karmic blockages."
            ),
            correctIndex = 2,
            topic = "Jain Philosophy"
        )
    )

    fun answerExamQuestion(questionIndex: Int, selectedOptionIndex: Int) {
        val currentAnswers = _examAnswers.value.toMutableMap()
        currentAnswers[questionIndex] = selectedOptionIndex
        _examAnswers.value = currentAnswers
    }

    fun nextExamQuestion() {
        if (_examQuestionIndex.value < examQuestions.size - 1) {
            _examQuestionIndex.value += 1
        }
    }

    fun prevExamQuestion() {
        if (_examQuestionIndex.value > 0) {
            _examQuestionIndex.value -= 1
        }
    }

    fun submitExam() {
        val answers = _examAnswers.value
        var correctCount = 0
        examQuestions.forEachIndexed { idx, q ->
            if (answers[idx] == q.correctIndex) {
                correctCount++
            }
        }

        // Must answer all 5 questions correctly to demonstrate intellectual depth!
        val passed = correctCount == examQuestions.size
        _examPassed.value = passed
        _examCompleted.value = true
        _showExamResult.value = true

        if (passed) {
            viewModelScope.launch {
                // Unlock unique host qualification badge
                repository.insertBadge(
                    UserBadge(
                        name = "Acharya Certification",
                        icon = "verified_user",
                        description = "Awarded for scoring 100% on the Prashna Philosophy, Logic, and Socratic Dialectic Rigorous Assessment."
                    )
                )
            }
        }
    }

    fun resetExam() {
        _examQuestionIndex.value = 0
        _examAnswers.value = emptyMap()
        _examCompleted.value = false
        _examPassed.value = false
        _showExamResult.value = false
    }

    fun dismissPostCall() {
        _isPostCallAnalysisActive.value = false
        _sessionAnalysisResult.value = null
    }

    fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}

data class ExamQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val topic: String
)
