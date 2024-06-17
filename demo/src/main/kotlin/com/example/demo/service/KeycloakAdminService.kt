package com.example.demo.service

import com.example.demo.model.keycloak.AccessTokenResponse
import com.example.demo.model.keycloak.Credential
import com.example.demo.model.keycloak.Realm
import com.example.demo.model.keycloak.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.http.auth.AuthenticationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.lang.reflect.Type
import java.net.ConnectException


@Service

class KeycloakAdminService {
    private val keycloakUrl: String = "http://localhost:8080"
    private val keycloakAdminUsername = "admin"
    private val keycloakAdminPassword = "admin"

    private val restTemplate = RestTemplate()
    private val gson = Gson()

    var logger: Logger = LoggerFactory.getLogger(KeycloakAdminService::class.java)

    @Retryable(
        value = [ConnectException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000L)
    )
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

        val responseEntity: ResponseEntity<String> =
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String::class.java)

        if (responseEntity.statusCode != HttpStatus.OK) {
            logger.error("Failed to obtain access token: ${responseEntity.statusCode}, ${responseEntity.body}")
        }

        val tokenResponse = gson.fromJson(responseEntity.body!!, AccessTokenResponse::class.java)
        return tokenResponse.access_token
    }
    @Retryable(
        value = [ConnectException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000L)
    )
    fun getRealms(accessToken: String):List<String> {
        val url = "$keycloakUrl/admin/realms"
        val headers = HttpHeaders()
        headers["Authorization"] = "Bearer $accessToken"

        val requestEntity = HttpEntity<Any>(headers)

        val responseEntity: ResponseEntity<String> =
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, String::class.java)
        if (responseEntity.statusCode != HttpStatus.OK) {
            logger.error("Failed to obtain access token: ${responseEntity.statusCode}, ${responseEntity.body}")
        }

        val listType: Type = object : TypeToken<List<Realm>>() {}.type
        val realmResponse : List<Realm> = gson.fromJson(responseEntity.body!!, listType)
        return realmResponse.map{ it.realm }.filter { it != "master" }
    }

    @Retryable(
        value = [ConnectException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000L)
    )
    fun createUser(accessToken: String, realm: String, user: User): Boolean {
        val url = "$keycloakUrl/admin/realms/$realm/users"
        val headers = HttpHeaders()
        headers["Authorization"] = "Bearer $accessToken"

        val requestEntity = HttpEntity(user, headers)

        val responseEntity: ResponseEntity<String> =
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String::class.java)

        if (responseEntity.statusCode != HttpStatus.CREATED) {
            logger.error("Failed to create user: ${responseEntity.statusCode}, ${responseEntity.body}")
            return false
        }
        logger.info("Create user success. ")
        return true
    }

    @Retryable(
        value = [ConnectException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000L)
    )
    fun getUser(accessToken: String, realm: String, username: String, params: Map<String, Any> = mapOf()): List<User> {
        var url = "$keycloakUrl/admin/realms/$realm/users"
        if (params.isNotEmpty()){
            url = "$url?${params.map { "${it.key}=${it.value}" }.joinToString("&")}"
        }
        val headers = HttpHeaders()
        headers["Authorization"] = "Bearer $accessToken"

        val requestEntity = HttpEntity<Any>(headers)

        val responseEntity: ResponseEntity<String> =
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, String::class.java)
        if (responseEntity.statusCode != HttpStatus.OK) {
            logger.error("Failed to obtain user: ${responseEntity.statusCode}, ${responseEntity.body}")
        }

        val listType: Type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(responseEntity.body!!, listType)
    }

    @Retryable(
        value = [ConnectException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000L)
    )
    fun deleteUser(accessToken: String, realm: String, userId: String): Boolean {
        val url = "$keycloakUrl/admin/realms/$realm/users/$userId"
        val headers = HttpHeaders()
        headers["Authorization"] = "Bearer $accessToken"

        val requestEntity = HttpEntity<Any>(headers)

        val responseEntity: ResponseEntity<String> =
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String::class.java)

        if (responseEntity.statusCode != HttpStatus.NO_CONTENT) {
            logger.error("Failed to delete user: ${responseEntity.statusCode}, ${responseEntity.body}")
            return false
        }
        logger.info("Delete user success. ")
        return true
    }

    @Retryable(
        value = [ConnectException::class, AuthenticationException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000L)
    )
    fun updateCredential(accessToken: String, realm: String, userId: String, credential: Credential): Boolean {
        try {
            val url = "$keycloakUrl/admin/realms/$realm/users/$userId/reset-password"
            val headers = HttpHeaders()
            headers.setBearerAuth(accessToken)
            val requestEntity = HttpEntity(credential, headers)
            val responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String::class.java)
            if (responseEntity.statusCode != HttpStatus.NO_CONTENT) {
                logger.error("Failed to update user credential: ${responseEntity.statusCode}, ${responseEntity.body}")
                return false
            }
            logger.info("Update user credential success. ")
            return true
        } catch (e: Exception){
            when (e) {
                is ConnectException -> throw e
                is HttpStatusCodeException -> {
                    logger.info("Failed to update user credential: ${e.statusCode}, ${e.responseBodyAsString}")
                    if (e.statusCode == HttpStatus.UNAUTHORIZED) {
                        logger.info("refresh token")
                        throw AuthenticationException()
                    }
                }
                else -> {
                    logger.info("Failed to update user credential: ", e)
                }
            }
        }
        return false
    }
}