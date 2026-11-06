
package com.example.campusconnectsa.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // STUDENT REGISTRATION
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    // STUDENT OR ADMIN LOGIN
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    // GOOGLE SIGN-IN
    @POST("api/auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<AuthResponse>

    // GET LOGGED-IN USER
    @GET("api/auth/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    // UPDATE PROFILE
    @PUT("api/auth/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Response<AuthResponse>

    // GET ALL ACADEMIC TASKS
    @GET("api/tasks")
    suspend fun getTasks(
        @Header("Authorization") token: String
    ): Response<TaskListResponse>

    // CREATE ACADEMIC TASK
    @POST("api/tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Body request: AcademicTaskRequest
    ): Response<TaskMutationResponse>

    // UPDATE ACADEMIC TASK
    @PUT("api/tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") taskId: String,
        @Body request: AcademicTaskUpdateRequest
    ): Response<TaskMutationResponse>

    // DELETE ACADEMIC TASK
    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") taskId: String
    ): Response<TaskMutationResponse>
}
