
package com.example.campusconnectsa.data

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val studentNumber: String,
    val password: String,
    val confirmPassword: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserData(
    val id: String,
    val fullName: String,
    val email: String,
    val studentNumber: String?,
    val role: String
)

data class AuthResponse(
    val status: String,
    val message: String,
    val token: String?,
    val user: UserData?
)

data class ApiError(
    val status: String?,
    val message: String?
)

// Create academic task
data class AcademicTaskRequest(
    val title: String,
    val taskType: String,
    val module: String,
    val priority: String,
    val dueDate: String
)

// Update academic task
data class AcademicTaskUpdateRequest(
    val title: String? = null,
    val taskType: String? = null,
    val module: String? = null,
    val priority: String? = null,
    val dueDate: String? = null,
    val completed: Boolean? = null
)

// Academic task returned by MongoDB
data class AcademicTaskData(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val taskType: String,
    val module: String?,
    val priority: String,
    val dueDate: String,
    val completed: Boolean
)

// Response containing all student tasks
data class TaskListResponse(
    val status: String,
    val count: Int,
    val tasks: List<AcademicTaskData>
)

// Response after creating or updating a task
data class TaskMutationResponse(
    val status: String,
    val message: String,
    val task: AcademicTaskData?
)

// Profile update
data class ProfileUpdateRequest(
    val fullName: String,
    val email: String,
    val studentNumber: String
)

// Google Sign-In request sent to the backend
data class GoogleLoginRequest(
    val idToken: String
)
