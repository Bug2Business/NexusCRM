package com.android.nexuscrm.model

// --- CRM DATA MODELS ---

enum class UserRole { ADMIN, USER, MANAGER }
data class User(
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true
)

enum class LeadStatus { NEW, CONTACTED, PROPOSAL, CLOSED }
data class Lead(
    val id: Int = 0,
    val name: String = "",
    val company: String = "",
    val value: Double = 0.0,
    val status: LeadStatus = LeadStatus.NEW,
    val phone: String = "",
    val email: String = "",
    val notes: MutableList<Interaction> = mutableListOf()
)

data class Interaction(
    val type: String = "", // Call, Meeting, Email, Note
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class CRMTask(
    val id: Int = 0,
    val title: String = "",
    val deadline: String = "",
    val isCompleted: Boolean = false,
    val contactName: String = ""
)

data class ActivityLog(
    val id: Int = 0,
    val action: String = "",
    val user: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
