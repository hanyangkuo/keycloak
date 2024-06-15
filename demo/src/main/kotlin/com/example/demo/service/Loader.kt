package com.example.demo.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Loader {
    @Autowired
    private lateinit var keycloakAdminService: KeycloakAdminService
}