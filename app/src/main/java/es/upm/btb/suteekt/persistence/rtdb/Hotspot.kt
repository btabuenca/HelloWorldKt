package es.upm.btb.suteekt.persistence.rtdb
data class Hotspot(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val report: String = "",
    val timestamp: Long = 0,
    val userId: String = "",
    var distance: Double = 0.0
)
