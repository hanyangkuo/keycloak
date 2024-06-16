package com.example.demo.model.keycloak
data class User(
    val id: String ="",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val enabled: Boolean = true,
    val attributes: Map<String, Array<Any>> = mapOf(),
    val credentials: List<Credential> = listOf(),
)

