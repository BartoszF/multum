package pl.felis.multum.common

object MultumPaths {
    const val Service = "service"
    const val Register = "register"
    const val Heartbeat = "heartbeat"
    const val Bye = "bye"
    val ServiceRegex = "/$Service/([a-zA-Z0-9][a-zA-Z0-9\\-]*)/(.*)".toRegex()
}
