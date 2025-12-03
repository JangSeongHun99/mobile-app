package com.example.myapp.models

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val icon: String = "🏷️",
    val color: String = "#6200EE" // Material Purple
)

/**
 * 기본 카테고리 목록 (TOEIC 도메인 중심)
 */
object DefaultCategories {
    val ALL = Category(id = "all", name = "전체", icon = "✨")
    val BUSINESS = Category(id = "business", name = "비즈니스/사무", icon = "🏢")
    val FINANCE = Category(id = "finance", name = "회계/금융", icon = "💰")
    val HR = Category(id = "hr", name = "인사/채용", icon = "👥")
    val MARKETING = Category(id = "marketing", name = "영업/마케팅", icon = "📣")
    val OPERATIONS = Category(id = "operations", name = "운영/생산/물류", icon = "🏭")
    val TECHNOLOGY = Category(id = "technology", name = "기술/IT", icon = "💻")
    val TRAVEL = Category(id = "travel", name = "출장/여행/교통", icon = "✈️")
    val HOSPITALITY = Category(id = "hospitality", name = "숙박/외식/서비스", icon = "🍽️")
    val LEGAL = Category(id = "legal", name = "법무/계약", icon = "⚖️")
    val HEALTH = Category(id = "health", name = "건강/안전/의료", icon = "🩺")
    val EDUCATION = Category(id = "education", name = "교육/연수", icon = "📚")
    val ENVIRONMENT = Category(id = "environment", name = "환경/에너지", icon = "🌿")
    val REAL_ESTATE = Category(id = "real_estate", name = "부동산/시설", icon = "🏠")
    val UNCATEGORIZED = Category(id = "uncategorized", name = "미분류", icon = "❔")

    fun getDefaultList(): List<Category> = listOf(
        ALL,
        BUSINESS,
        FINANCE,
        HR,
        MARKETING,
        OPERATIONS,
        TECHNOLOGY,
        TRAVEL,
        HOSPITALITY,
        LEGAL,
        HEALTH,
        EDUCATION,
        ENVIRONMENT,
        REAL_ESTATE,
        UNCATEGORIZED
    )

    fun findById(id: String): Category? = getDefaultList().find { it.id == id }
}
