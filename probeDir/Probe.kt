import java.time.LocalDateTime
fun main() {
    val t = LocalDateTime.of(2026, 11, 8, 0, 12, 30)
    listOf("yy","MM","dd","HH","mm","yyMMddHHmm","yyyy.MM.dd.HHmm").forEach {
        println("PROBE [] -> [" + t.format(java.time.format.DateTimeFormatter.ofPattern(it)) + "]")
    }
}
