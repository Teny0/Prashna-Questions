package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrashnaRepository(private val db: AppDatabase) {
    val allHosts: Flow<List<Host>> = db.hostDao().getAllHosts()
    val allJournalEntries: Flow<List<JournalEntry>> = db.journalEntryDao().getAllEntries()
    val allSessions: Flow<List<Session>> = db.sessionDao().getAllSessions()
    val allBadges: Flow<List<UserBadge>> = db.userBadgeDao().getAllBadges()

    suspend fun insertJournalEntry(entry: JournalEntry) = withContext(Dispatchers.IO) {
        db.journalEntryDao().insertEntry(entry)
    }

    suspend fun deleteJournalEntry(entry: JournalEntry) = withContext(Dispatchers.IO) {
        db.journalEntryDao().deleteEntry(entry)
    }

    suspend fun insertSession(session: Session) = withContext(Dispatchers.IO) {
        db.sessionDao().insertSession(session)
    }

    suspend fun insertBadge(badge: UserBadge) = withContext(Dispatchers.IO) {
        db.userBadgeDao().insertBadge(badge)
    }

    suspend fun hasBadge(name: String): Boolean = withContext(Dispatchers.IO) {
        db.userBadgeDao().hasBadge(name) > 0
    }

    suspend fun prePopulateIfEmpty() = withContext(Dispatchers.IO) {
        val hostCount = db.hostDao().getHostCount()
        if (hostCount == 0) {
            val starterHosts = listOf(
                Host(
                    name = "Acharya Shunya",
                    specialties = "Principal Upanishads, Advaita Vedanta, Socratic Questioning",
                    rating = 4.9,
                    pricingPerMinute = 4.5,
                    wisdomScore = 98,
                    trustScore = 99,
                    bio = "Spiritual teacher specializing in Upanishadic non-duality (Advaita) and helping seekers deconstruct the ego through self-inquiry."
                ),
                Host(
                    name = "Swami Jnanananda",
                    specialties = "Bhagavad Gita, Yoga Philosophy, Indian Philosophy",
                    rating = 4.8,
                    pricingPerMinute = 3.5,
                    wisdomScore = 95,
                    trustScore = 97,
                    bio = "Contemplative monk with 25 years of monastic experience. Teaches practical application of the Gita's wisdom in dynamic, fast-paced modern lives."
                ),
                Host(
                    name = "Dr. Mira Sen",
                    specialties = "Buddhist Philosophy, Logic (Nyaya), Critical Thinking",
                    rating = 4.95,
                    pricingPerMinute = 5.0,
                    wisdomScore = 99,
                    trustScore = 98,
                    bio = "Oxford-educated Sanskrit scholar. Specializes in Buddhist logic, Madhyamaka emptiness, and applying rigorous logic to existential questions."
                ),
                Host(
                    name = "Prof. K. Raghavan",
                    specialties = "Sankhya Philosophy, Jain Philosophy, Ethics",
                    rating = 4.7,
                    pricingPerMinute = 3.0,
                    wisdomScore = 92,
                    trustScore = 95,
                    bio = "Academic researcher and ethicist. Helps seekers discover dualistic discrimination (Prakriti vs Purusha) and non-absolutism (Anekantavada)."
                ),
                Host(
                    name = "Bodhidharma Dev",
                    specialties = "Zen Philosophy, Active Listening, Perspective Taking",
                    rating = 4.85,
                    pricingPerMinute = 4.0,
                    wisdomScore = 94,
                    trustScore = 96,
                    bio = "Audio dialogue practitioner. Uses Zen koans and deep, active silence to help you hear what you aren't saying."
                )
            )
            db.hostDao().insertHosts(starterHosts)
        }
    }
}
