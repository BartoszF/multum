package pl.felis.multum.common.dao

import kotlinx.serialization.Serializable

@Serializable
data class RegisterData(val port: Int, val prometheusMetrics: Boolean = true)
