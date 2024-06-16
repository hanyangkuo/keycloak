package com.example.demo.service

import com.example.demo.model.keycloak.Credential
import com.example.demo.model.keycloak.User
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.charset.StandardCharsets
import java.util.stream.IntStream


@SpringBootTest
class KeycloakAdminServiceTest {
    var logger: Logger = LoggerFactory.getLogger(KeycloakAdminServiceTest::class.java)

    @Autowired
    private lateinit var keycloakAdminService: KeycloakAdminService

    @Autowired
    private lateinit var hashService: HashService

    @Test
    fun testGetAccessToken(){
        val accessToken = keycloakAdminService.getAccessToken()
        logger.info(accessToken)

        val realms = keycloakAdminService.getRealms(accessToken)
        logger.info("$realms")

        val user = User(
            username = "test",
            email = "test@gmail.com",
            firstName = " ",
            lastName = "Kuo",
            credentials = listOf(Credential(
                value = "hello"
            ))
        )
        val result = keycloakAdminService.createUser(accessToken, realms[0], user)
        logger.info("create user $result")

        val users = keycloakAdminService.getUser(
            accessToken, realms[0],
            "tw01472760",
            mapOf(
                "username" to "test",
                "exact" to true,
            )
        )
        logger.info("get users: $users")
        if (users.isNotEmpty()) {
            val credential = Credential(
                value = "testtest"
            )
            keycloakAdminService.updateCredential(accessToken, realms[0], users[0].id, credential)
        }

        if (users.isNotEmpty()) {
            keycloakAdminService.deleteUser(accessToken, realms[0], users[0].id)
        }
    }

    @Test
    fun testArgon2(){
//        val hash = hashService.argon2i("hello", "helloworld".toByteArray(StandardCharsets.UTF_8))
//
//        logger.info("Hash: $hash")
//
        val i=1
        IntStream.range(1, 13).forEach{
            val seed = hashService.getSeed(2024, it)
            val hash = hashService.argon2i("hello", seed)
            logger.info("month: $it, seed=$seed, hash=$hash")
        }
    }

}