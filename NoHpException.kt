package plugin.koms.calculator

class NoHpException(
    msg: String = "",
    val result: StageResult, val v: Double
) : RuntimeException(msg)