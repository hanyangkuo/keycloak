package com.example.demo.service

import com.example.demo.model.keycloak.AccessTokenResponse
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KeycloakAdminService {
    private val keycloakUrl: String = "http://localhost:8080"
    private val keycloakAdminUsername = "admin"
    private val keycloakAdminPassword = "admin"

    private val restTemplate = RestTemplate()
    private val gson = Gson()

    fun getAccessToken(): String {
        val url = "$keycloakUrl/realms/master/protocol/openid-connect/token"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val body = mapOf(
            "client_id" to "admin-cli",
            "username" to keycloakAdminUsername,
            "password" to keycloakAdminPassword,
            "grant_type" to "password"
        )

        val requestEntity = HttpEntity(body.map { "${it.key}=${it.value}" }.joinToString("&"), headers)

        val responseEntity: ResponseEntity<String> = restTemplate.postForEntity(url, requestEntity, String::class.java)

        if (responseEntity.statusCode != HttpStatus.OK) {
            throw RuntimeException("Failed to obtain access token: ${responseEntity.body}")
        }

        val tokenResponse = gson.fromJson(responseEntity.body!!, AccessTokenResponse::class.java)
        return tokenResponse.access_token
    }



}