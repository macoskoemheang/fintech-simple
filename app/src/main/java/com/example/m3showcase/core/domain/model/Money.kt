package com.example.m3showcase.core.domain.model

/**
 * Money stored as minor units (cents for USD, riel for KHR) so we never do
 * floating-point arithmetic on balances.
 */
data class Money(val minor: Long, val currency: CurrencyCode) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch" }
        return copy(minor = minor + other.minor)
    }

    operator fun minus(other: Money): Money = plus(other.copy(minor = -other.minor))

    operator fun unaryMinus(): Money = copy(minor = -minor)
}

enum class CurrencyCode(val symbol: String, val fractionDigits: Int) {
    USD("$", 2),
    KHR("៛", 0),
}
