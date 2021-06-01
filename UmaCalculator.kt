package plugin.koms.calculator

import kotlin.math.*

/**
 * 马娘计算器，输入各项参数后使用 [calculate] 方法计算结果。
 */
class UmaCalculator {
    // 面板属性
    var speed = 0
    var stamina = 0
    var power = 0
    var guts = 0
    var wisdom = 0

    /**
     * 干劲补正，1.04 1.02 1 0.98 0.96
     */
    var happy = 1.0

    /**
     * 场地速度补正 不良 -50，其余 0
     */
    var groundSpeed = 0

    /**
     * 场地力量补正 良 0，其余 -50，泥地再 -50
     */
    var groundPower = 0

    /**
     * 跑法智力补正 S 1.1, A 1, B 0.85
     */
    var tacticsWisdom = 1.0

    /**
     * 总距离
     */
    var distance = 1000


    // 补正后属性
    var aSpeed = 0.0
    var aStamina = 0.0
    var aPower = 0.0
    var aGuts = 0.0
    var aWisdom = 0.0

    /**
     * 计算实际属性
     */
    private fun calculateStats() {
        aSpeed = speed * happy + groundSpeed
        aStamina = stamina * happy
        aPower = power * happy + groundPower
        aGuts = guts * happy
        aWisdom = wisdom * happy * tacticsWisdom
    }

    // 以上是基本属性部分

    /**
     * 跑法 1 - 逃, 2 - 先, 3 - 差, 4 - 追
     */
    var tact = 1

    /**
     * 跑法hp补正表
     */
    private val tactHp = mapOf(1 to 0.95, 2 to 0.89, 3 to 1.0, 4 to 0.995)

    /**
     * 跑法序盘速度补正表
     */
    private val tactSpeed1 = mapOf(1 to 1.0, 2 to 0.978, 3 to 0.938, 4 to 0.931)

    /**
     * 跑法中盘速度补正表
     */
    private val tactSpeed2 = mapOf(1 to 0.98, 2 to 0.991, 3 to 0.998, 4 to 1.0)

    /**
     * 跑法终盘速度补正表
     */
    private val tactSpeed3 = mapOf(1 to 0.962, 2 to 0.975, 3 to 0.994, 4 to 1.0)

    /**
     * 跑法序盘加速度补正表
     */
    private val tactAcceleration1 = mapOf(1 to 1.0, 2 to 0.985, 3 to 0.975, 4 to 0.945)

    /**
     * 跑法中盘加速度补正表
     */
    private val tactAcceleration2 = mapOf(1 to 1.0, 2 to 1.0, 3 to 1.0, 4 to 1.0)

    /**
     * 跑法终盘加速度补正表
     */
    private val tactAcceleration3 = mapOf(1 to 0.996, 2 to 0.996, 3 to 1.0, 4 to 0.997)

    /**
     * 场地情况hp消耗系数，良，稍 1, 重 芝 1.02 泥 1.01, 不良 1.02
     */
    var groundHp = 1.0

    /**
     * 距离适性速度补正 S 1.05, A 1, B 0.9
     */
    var distanceSpeed = 1.0

    /**
     * 场地适性加速度补正 S 1.05, A 1, B 0.9
     */
    var groundAcceleration = 1.0

    /**
     * 距离适性加速度补正 S-D 都是 1
     */
    var distanceAcceleration = 1.0

    /**
     * 技能得到的hp，金550，白150
     */
    var skillHp = 0.0


    /**
     * 终盘hp消耗系数
     */
    var finalHp = 0.0

    /**
     * HP
     */
    var hp = 0.0

    /**
     * 基准速度，只和距离有关
     */
    var v0 = 0.0

    /**
     * 序盘巡航速度
     */
    var v1 = 0.0

    /**
     * 中盘巡航速度
     */
    var v2 = 0.0

    /**
     * 终盘巡航速度
     */
    var v3 = 0.0

    /**
     * 最终冲刺速度
     */
    var v4 = 0.0

    /**
     * 起跑到 0.85v0 的加速度
     */
    var a0 = 0.0

    /**
     * 从0.85v0加速到v1的加速度
     */
    var a1 = 0.0

    /**
     * v1加速到v2的加速度
     */
    var a2 = 0.0

    /**
     * v2加速到v3的加速度
     */
    var a3 = 0.0

    /**
     * 最终冲刺加速度
     */
    var af = 0.0

    /**
     * 计算各项参数。
     */
    private fun calculateVA() {
        v0 = 20 - (distance - 2000) / 1000.0
        v1 = (tactSpeed1[tact]!! + (aWisdom * log10(aWisdom / 10.0)) / 550000.0 - 0.00325) * v0
        v2 = (tactSpeed2[tact]!! + (aWisdom * log10(aWisdom / 10.0)) / 550000.0 - 0.00325) * v0
        v3 =
            (tactSpeed3[tact]!! + (aWisdom * log10(aWisdom / 10.0)) / 550000.0 - 0.00325) * v0 + sqrt(aSpeed * 500) * distanceSpeed * 0.002
        v4 =
            ((tactSpeed3[tact]!! + 0.01) * v0 + sqrt(aSpeed * 500) * distanceSpeed * 0.002) * 1.05 + sqrt(aSpeed * 500) * distanceSpeed * 0.002

        a0 = 24 + 0.0006 * sqrt(500 * aPower) * tactAcceleration1[tact]!! * groundAcceleration * distanceAcceleration
        a1 = 0.0006 * sqrt(500 * aPower) * tactAcceleration1[tact]!! * groundAcceleration * distanceAcceleration
        a2 = if (v2 > v1) {
            0.0006 * sqrt(500 * aPower) * tactAcceleration2[tact]!! * groundAcceleration * distanceAcceleration
        } else {
            -0.8
        }
        a3 = if (v3 > v2) {
            0.0006 * sqrt(500 * aPower) * tactAcceleration3[tact]!! * groundAcceleration * distanceAcceleration
        } else {
            -0.8
        }
        af = 0.0006 * sqrt(500 * aPower) * tactAcceleration3[tact]!! * groundAcceleration * distanceAcceleration

        finalHp = 1 + 200 / sqrt(600.0 * aGuts)
        hp = (distance + 0.8 * aStamina * tactHp[tact]!!) * (1 + skillHp / 10000.0)
        minSpeed = v0 * 0.85 + sqrt(200 * aGuts) * 0.001
    }


    /**
     * 巡航时hp消耗
     * @param coefficient 额外系数，终盘时为 [finalHp]
     * @param v 速度
     * @param t 时间
     */
    private fun getHpConsumeCruise(v: Double, t: Double, coefficient: Double = 1.0) =
        20.0 * groundHp * coefficient * (((v - v0 + 12).pow(2.0)) / 144) * t

    /**
     * 加速时hp消耗
     * @param coefficient 额外系数，终盘时为 [finalHp]
     * @param v 初速度
     * @param a 加速度
     * @param t 时间
     */
    private fun getHpConsumeAcceleration(v: Double, t: Double, a: Double, coefficient: Double = 1.0) =
        20.0 * groundHp * coefficient * (((a * t + v - v0 + 12).pow(3) - (v - v0 + 12).pow(3)) / (432 * a))


    /**
     * 最小目标速度
     */
    var minSpeed = 0.0

    // 下面是计算部分

    /**
     * 总时间
     */
    var totalTime = 0.0

    /**
     * 总距离
     */
    var totalDistance = 0.0

    /**
     * 总hp消耗
     */
    var totalHpConsume = 0.0

    var resultMap = mutableMapOf<String, PhaseResult>()

    /**
     * 计算加速过程
     * @param v0 初速
     * @param v1 目标速度
     * @param a 加速度
     * @param isFinal 是否是终盘
     */
    private fun calculateAcceleration(v0: Double, v1: Double, a: Double, isFinal: Boolean = false): PhaseResult {
        val res = PhaseResult()
        val time = abs((v1 - v0) / a)

        res.time = time
        val distance = v0 * time + 0.5 * a * time * time
        res.distance = distance

        if (isFinal) {
            res.hp = getHpConsumeAcceleration(v0, time, a, finalHp)
        } else {
            res.hp = getHpConsumeAcceleration(v0, time, a)
        }

        if (totalHpConsume + res.hp >= hp) {
            val reHp = hp - totalHpConsume
            var mt = 0.0
            for (i in 0..(time.toInt() + 10) * 100) { // 计算失速时间（精度0.01）
                if (getHpConsumeAcceleration(v0, i / 100.0, a, finalHp) >= reHp) {
                    mt = i / 100.0
                    break
                }
            }

            val md = v0 * mt + 0.5 * a * mt * mt
            res.time = mt
            res.hp = reHp
            res.distance = md

            totalTime += mt
            totalDistance += md
            totalHpConsume = hp

            throw NoHpException("${totalTime}时失速。", res, v0 + a * mt)
        }

        totalTime += time
        totalDistance += distance
        totalHpConsume += res.hp
        return res
    }

    /**
     * 计算巡航过程
     * @param v 速度
     * @param d 距离
     * @param isFinal 是否是终盘
     */
    private fun calculateCruise(v: Double, d: Double, isFinal: Boolean = false): PhaseResult {
        val res = PhaseResult()
        res.time = d / v
        res.distance = d
        if (isFinal) {
            res.hp = getHpConsumeCruise(v, d / v, finalHp)
        } else {
            res.hp = getHpConsumeCruise(v, d / v)
        }

        if (totalHpConsume + res.hp >= hp) {
            val reHp = hp - totalHpConsume
            var mt = 0.0
            for (i in 0..(res.time.toInt() + 10) * 100) { // 计算失速时间（精度0.01）
                if (getHpConsumeCruise(v, i / 100.0, finalHp) >= reHp) {

                    mt = i / 100.0
                    break
                }
            }

            res.time = mt
            res.distance = mt * v
            res.hp = reHp

            totalTime += mt
            totalDistance += mt * v
            totalHpConsume = hp

            throw NoHpException("${totalTime}时失速。", res, v)
        }

        totalTime += res.time
        totalDistance += d
        totalHpConsume += res.hp
        return res
    }

    /**
     * 失速了
     * @param v 速度
     */
    private fun muuRii(v: Double): PhaseResult {
        val res = PhaseResult()
        val d = distance - totalDistance
        res.distance = d
        res.hp = 0.0
        val time = (v - minSpeed) / 1.2

        val ad = v * time - 0.6 * time * time

        if (ad < d) {
            res.time = time + (d - ad) / minSpeed
        } else {
            val at = (v - sqrt(v * v - 2.4 * d)) / 1.2
            res.time = at
        }

        totalTime += res.time
        totalDistance += res.distance
        return res
    }

    /**
     * 最终冲刺距离
     */
    var df = 0.0

    /**
     * 计算结果，结果存在 [resultMap] 中。
     * @throws CalculatorException 出现异常情况
     */
    fun calculate() {
        resultMap.clear()
        totalTime = 0.0
        totalDistance = 0.0
        totalHpConsume = 0.0

        calculateStats()
        calculateVA()

        val start = calculateAcceleration(3.0, 0.85 * v0, a0)
        resultMap["start"] = start

        val sa0 = calculateAcceleration(0.85 * v0, v1, a1)
        resultMap["phase0-a"] = sa0

        val sc0 = calculateCruise(v1, distance / 6 - totalDistance)
        resultMap["phase0-c"] = sc0

        val sa1 = calculateAcceleration(v1, v2, a2)
        resultMap["phase1-a"] = sa1

        val sc1 = calculateCruise(v2, distance * 2 / 3 - totalDistance)
        resultMap["phase1-c"] = sc1

        df = min(
            distance / 3.0,
            (hp - totalHpConsume - (distance / 3 - 60) * 20 * finalHp * groundHp * (v3 - v0 + 12).pow(2) / 144 / v3) /
                    (20 * finalHp * groundHp * ((v4 - v0 + 12).pow(2) / 144 / v4 - (v3 - v0 + 12).pow(2) / 144 / v3)) + 60
        )

        if (df <= 0) {
            throw CalculatorException("最终冲刺距离小于0！这马已经没救了。")
        }

        var cur = ""
        try {
            if (df + totalDistance < distance) {
                cur = "phase2-a"
                val sa2 = calculateAcceleration(v2, v3, a3, true)
                resultMap[cur] = sa2
            }

            if (df + totalDistance < distance) {
                cur = "phase2,3-c"
                val sc23 = calculateCruise(v3, distance - df - totalDistance, true)
                resultMap[cur] = sc23
            }

            cur = "last-a"
            val sa3 = calculateAcceleration(v3, v4, af, true)
            resultMap[cur] = sa3

            cur = "last-c"
            val sc3 = calculateCruise(v4, distance - totalDistance, true)
            resultMap[cur] = sc3
        } catch (e: NoHpException) {
            resultMap[cur] = e.result
            val mm = muuRii(e.v)
            resultMap["MuuRii"] = mm
        }
    }
}