package com.example.demo.service

import org.apache.commons.codec.digest.DigestUtils
import org.bouncycastle.crypto.generators.Argon2BytesGenerator

import org.bouncycastle.crypto.params.Argon2Parameters
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.floor

@Service
class HashService {
    val iterations = 2
    val memLimit = 66536
    val hashLength = 16
    val parallelism = 1
    val divisor =6

    fun getSeed(year: Int, month: Int): ByteArray {
        return DigestUtils.md5(year.toString() + (month-1).floorDiv(divisor).toString())
    }
    fun argon2i(password: String, salt: ByteArray): String {
//        val calendar = Calendar.getInstance()
//        val year = calendar[Calendar.YEAR]
//        val month = calendar[Calendar.MONTH]
//        val salt = getSeed(year, month).toByteArray()
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_i)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(iterations)
            .withMemoryAsKB(memLimit)
            .withParallelism(parallelism)
            .withSalt(salt)

        val result = ByteArray(hashLength)

        val generate = Argon2BytesGenerator()
        generate.init(builder.build())
        generate.generateBytes(password.toByteArray(StandardCharsets.UTF_8), result, 0, result.size)
        return Base64.getEncoder().encodeToString(result).replace("=","")
    }

}