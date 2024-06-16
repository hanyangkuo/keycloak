package com.example.demo.model.keycloak

data class Credential(
    val type: String = "password",
    val value: String = "",
    val temporary: Boolean = false,
)