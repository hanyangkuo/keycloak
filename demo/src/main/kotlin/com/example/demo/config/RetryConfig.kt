package com.example.demo.config

import org.springframework.classify.Classifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus.*
import org.springframework.http.HttpStatusCode
import org.springframework.retry.RetryPolicy
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy
import org.springframework.retry.policy.NeverRetryPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.web.client.HttpStatusCodeException


@Configuration
class RetryConfig {
    private val simpleRetryPolicy: SimpleRetryPolicy = SimpleRetryPolicy(MAX_RETRY_ATTEMPTS)
    private val neverRetryPolicy: NeverRetryPolicy = NeverRetryPolicy()
    @Bean
    fun retryTemplate(): RetryTemplate {
        val retryTemplate = RetryTemplate()
        val policy = ExceptionClassifierRetryPolicy()
        policy.setExceptionClassifier(configureStatusCodeBasedRetryPolicy())
        retryTemplate.setRetryPolicy(policy)
        return retryTemplate
    }

    private fun configureStatusCodeBasedRetryPolicy(): Classifier<Throwable, RetryPolicy> {
        return Classifier<Throwable, RetryPolicy> { throwable ->
            if (throwable is HttpStatusCodeException) {
                return@Classifier getRetryPolicyForStatus(throwable.statusCode)
            }
            simpleRetryPolicy
        }
    }

    private fun getRetryPolicyForStatus(httpStatus: HttpStatusCode): RetryPolicy {
        return when (httpStatus) {
            BAD_GATEWAY, SERVICE_UNAVAILABLE, INTERNAL_SERVER_ERROR, GATEWAY_TIMEOUT -> simpleRetryPolicy
            else -> neverRetryPolicy
        }
    }

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 2
    }
}