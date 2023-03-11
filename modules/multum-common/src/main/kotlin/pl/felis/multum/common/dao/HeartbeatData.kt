package pl.felis.multum.common.dao

import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatData(val port: Int)
