package com.example.demo.service

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class KeycloakAdminServiceTest {
    var logger: Logger = LoggerFactory.getLogger(KeycloakAdminServiceTest::class.java)

    @Autowired
    private lateinit var keycloakAdminService: KeycloakAdminService

    @Test
    fun testGetAccessToken(){
        val token = keycloakAdminService.getAccessToken()
        logger.info("$token")
    }
}