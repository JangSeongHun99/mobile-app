package com.example.myapp.data

import com.example.myapp.models.DefaultCategories
import com.example.myapp.models.WordEntry

/**
 * 간단한 키워드 매칭으로 TOEIC 기본 어휘를 카테고리로 분류한다.
 * - 우선순위: 수동 매핑 > 카테고리별 키워드 > 기본(비즈니스)
 */
object ToeicCategoryClassifier {
    private val manualOverrides: Map<String, String> = mapOf(
        "banquet" to DefaultCategories.HOSPITALITY.id,
        "ingredient" to DefaultCategories.HOSPITALITY.id,
        "recipe" to DefaultCategories.HOSPITALITY.id,
        "beverage" to DefaultCategories.HOSPITALITY.id,
        "cuisine" to DefaultCategories.HOSPITALITY.id,
        "commute" to DefaultCategories.TRAVEL.id,
        "itinerary" to DefaultCategories.TRAVEL.id,
        "fare" to DefaultCategories.TRAVEL.id,
        "luggage" to DefaultCategories.TRAVEL.id,
        "warehouse" to DefaultCategories.OPERATIONS.id,
        "inventory" to DefaultCategories.OPERATIONS.id,
        "logistics" to DefaultCategories.OPERATIONS.id,
        "resume" to DefaultCategories.HR.id,
        "candidate" to DefaultCategories.HR.id,
        "invoice" to DefaultCategories.FINANCE.id,
        "receipt" to DefaultCategories.FINANCE.id,
        "tax" to DefaultCategories.FINANCE.id,
        "audit" to DefaultCategories.LEGAL.id,
        "regulation" to DefaultCategories.LEGAL.id,
        "contract" to DefaultCategories.LEGAL.id,
        "diagnosis" to DefaultCategories.HEALTH.id,
        "clinic" to DefaultCategories.HEALTH.id,
        "insurance" to DefaultCategories.FINANCE.id,
        "property" to DefaultCategories.REAL_ESTATE.id,
        "tenant" to DefaultCategories.REAL_ESTATE.id,
        "lease" to DefaultCategories.REAL_ESTATE.id
    )

    private val keywordRules: List<Pair<String, List<String>>> = listOf(
        DefaultCategories.TRAVEL.id to listOf(
            "travel", "trip", "tour", "journey", "itinerary", "luggage", "baggage",
            "fare", "ticket", "boarding", "departure", "arrival", "terminal",
            "commute", "shuttle", "vehicle", "driver", "garage", "parking",
            "bus", "train", "rail", "subway", "metro", "airline", "airport",
            "flight", "passport", "visa", "customs", "station", "route"
        ),
        DefaultCategories.HOSPITALITY.id to listOf(
            "hotel", "inn", "motel", "suite", "guest", "room", "housekeeping",
            "banquet", "catering", "dining", "restaurant", "cafe", "bar", "kitchen",
            "meal", "menu", "ingredient", "beverage", "cuisine", "chef", "cook", "reservation"
        ),
        DefaultCategories.FINANCE.id to listOf(
            "finance", "budget", "revenue", "expense", "earnings", "dividend",
            "payment", "payable", "receivable", "invoice", "receipt", "refund",
            "tax", "fee", "charge", "credit", "debit", "loan", "mortgage",
            "interest", "liability", "equity", "capital", "treasury", "fund",
            "premium", "valuation", "audit"
        ),
        DefaultCategories.HR.id to listOf(
            "employee", "employer", "staff", "personnel", "candidate", "applicant",
            "resume", "cv", "recruit", "hiring", "recruitment", "interview",
            "vacancy", "career", "training", "orientation", "intern", "internship",
            "salary", "wage", "payroll", "benefit", "retirement", "resignation"
        ),
        DefaultCategories.MARKETING.id to listOf(
            "market", "marketing", "promotion", "advert", "ad ", "campaign", "publicity",
            "brand", "brochure", "catalog", "customer", "client", "consumer",
            "sale", "sales", "retail", "merchandise", "discount", "coupon", "pricing",
            "survey", "audience", "launch", "release", "feature"
        ),
        DefaultCategories.OPERATIONS.id to listOf(
            "inventory", "stock", "supply", "warehouse", "shipping", "cargo", "freight",
            "delivery", "dispatch", "pack", "package", "logistics", "distribution",
            "factory", "plant", "assembly", "production", "manufacture", "equipment",
            "machine", "machinery", "device", "installation", "maintenance", "repair",
            "construction", "facility", "storage", "inspection", "quality", "defect",
            "warranty", "capacity", "raw material", "material"
        ),
        DefaultCategories.TECHNOLOGY.id to listOf(
            "tech", "software", "hardware", "device", "computer", "digital", "online",
            "internet", "web", "server", "system", "network", "data", "database",
            "cloud", "ai", "robot", "automation", "program", "code", "application",
            "security", "encryption", "password", "platform"
        ),
        DefaultCategories.LEGAL.id to listOf(
            "law", "legal", "compliance", "contract", "agreement", "policy", "regulation",
            "license", "permit", "liability", "litigation", "lawsuit", "court", "statute",
            "patent", "copyright", "trademark", "legislation", "ban"
        ),
        DefaultCategories.HEALTH.id to listOf(
            "health", "medical", "clinic", "hospital", "vaccine", "treatment", "medicine",
            "drug", "pharmacy", "patient", "surgery", "infection", "disease", "injury",
            "safety", "wellness", "hygiene"
        ),
        DefaultCategories.EDUCATION.id to listOf(
            "education", "academy", "school", "student", "training", "lesson", "course",
            "curriculum", "lecture", "seminar", "workshop", "certificate", "exam",
            "study", "learn", "tutor", "professor"
        ),
        DefaultCategories.ENVIRONMENT.id to listOf(
            "environment", "eco", "green", "sustain", "recycle", "waste", "emission",
            "pollution", "climate", "energy", "fuel", "power", "conservation", "renewable",
            "carbon"
        ),
        DefaultCategories.REAL_ESTATE.id to listOf(
            "property", "estate", "rent", "rental", "lease", "tenant", "landlord",
            "construction", "building", "renovation", "facility", "utility",
            "apartment", "housing", "residence", "real estate"
        )
    )

    fun classify(term: String, meaning: String = ""): String {
        val normalizedTerm = term.lowercase()
        val normalizedMeaning = meaning.lowercase()

        manualOverrides[normalizedTerm]?.let { return it }

        keywordRules.forEach { (categoryId, keywords) ->
            if (keywords.any { keyword ->
                    normalizedTerm.contains(keyword) || normalizedMeaning.contains(keyword)
                }
            ) {
                return categoryId
            }
        }

        return DefaultCategories.BUSINESS.id
    }

    fun apply(word: WordEntry): WordEntry =
        word.copy(categoryId = classify(word.term, word.meaning))
}
