
package com.example.campusconnectsa.data

class AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): AuthResponse {

        val response = ApiClient.apiService.login(
            LoginRequest(email, password)
        )

        if (response.isSuccessful) {
            return response.body()
                ?: throw Exception("Empty server response.")
        }

        throw Exception(
            response.errorBody()?.string()
                ?: "Login failed. Please try again."
        )
    }

    suspend fun register(
        fullName: String,
        email: String,
        studentNumber: String,
        password: String,
        confirmPassword: String
    ): AuthResponse {

        val response = ApiClient.apiService.register(
            RegisterRequest(
                fullName,
                email,
                studentNumber,
                password,
                confirmPassword
            )
        )

        if (response.isSuccessful) {
            return response.body()
                ?: throw Exception("Empty server response.")
        }

        throw Exception(
            response.errorBody()?.string()
                ?: "Registration failed. Please try again."
        )
    }
}
