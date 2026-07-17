package com.supernova.networkswitch.domain.model

enum class ControlMethod {
    ROOT,
    SHIZUKU
}

enum class NetworkMode(val displayName: String, val value: Int) {
    // 基础模式
    GSM_ONLY("仅 2G（GSM）", 1),
    WCDMA_ONLY("仅 3G（WCDMA）", 2),
    LTE_ONLY("仅 4G（LTE）", 11),
    NR_ONLY("仅 5G（NR）", 23),

    // 首选模式
    WCDMA_PREF("2G/3G（优先 3G）", 0),
    GSM_UMTS("2G/3G（自动）", 3),

    // 组合模式
    LTE_GSM_WCDMA("2G/3G/4G（LTE/GSM/WCDMA）", 9),
    LTE_WCDMA("3G/4G（LTE/WCDMA）", 12),
    NR_LTE("4G/5G（NR/LTE）", 24),
    NR_LTE_GSM_WCDMA("2G/3G/4G/5G（NR/LTE/GSM/WCDMA）", 26),
    NR_LTE_WCDMA("3G/4G/5G（NR/LTE/WCDMA）", 28),

    // 美国运营商 CDMA 模式
    CDMA("CDMA（自动）", 4),
    CDMA_NO_EVDO("仅 CDMA", 5),
    EVDO_NO_CDMA("仅 EvDo", 6),
    LTE_CDMA_EVDO("CDMA/4G（LTE/CDMA/EvDo）", 8),
    NR_LTE_CDMA_EVDO("CDMA/4G/5G（NR/LTE/CDMA/EvDo）", 25),

    // 全球模式
    GLOBAL("全球（全部）", 7),
    LTE_CDMA_EVDO_GSM_WCDMA("全球 4G（LTE/CDMA/EvDo/GSM/WCDMA）", 10),
    NR_LTE_CDMA_EVDO_GSM_WCDMA("全球 5G（NR/LTE/CDMA/EvDo/GSM/WCDMA）", 27),

    // 中国 TD-SCDMA 模式
    TDSCDMA_ONLY("仅 TD-SCDMA", 13),
    TDSCDMA_WCDMA("3G（TD-SCDMA/WCDMA）", 14),
    LTE_TDSCDMA("4G（LTE/TD-SCDMA）", 15),
    TDSCDMA_GSM("2G/TD-SCDMA", 16),
    LTE_TDSCDMA_GSM("2G/4G（LTE/TD-SCDMA/GSM）", 17),
    TDSCDMA_GSM_WCDMA("2G/3G（TD-SCDMA/GSM/WCDMA）", 18),
    LTE_TDSCDMA_WCDMA("3G/4G（LTE/TD-SCDMA/WCDMA）", 19),
    LTE_TDSCDMA_GSM_WCDMA("2G/3G/4G（LTE/TD-SCDMA/GSM/WCDMA）", 20),
    TDSCDMA_CDMA_EVDO_GSM_WCDMA("全球 3G（TD-SCDMA/CDMA/EvDo/GSM/WCDMA）", 21),
    LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA("全球 4G（LTE/TD-SCDMA/CDMA/EvDo/GSM/WCDMA）", 22),
    NR_LTE_TDSCDMA("4G/5G（NR/LTE/TD-SCDMA）", 29),
    NR_LTE_TDSCDMA_GSM("2G/4G/5G（NR/LTE/TD-SCDMA/GSM）", 30),
    NR_LTE_TDSCDMA_WCDMA("3G/4G/5G（NR/LTE/TD-SCDMA/WCDMA）", 31),
    NR_LTE_TDSCDMA_GSM_WCDMA("2G/3G/4G/5G（NR/LTE/TD-SCDMA/GSM/WCDMA）", 32),
    NR_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA("全球 5G + TD-SCDMA（所有网络）", 33);

    companion object {
        /**
         * 通过 RIL 常量值获取 NetworkMode
         */
        fun fromValue(value: Int): NetworkMode? {
            return values().find { it.value == value }
        }
    }
}

/**
 * 可在两种模式之间切换的配置
 */
data class ToggleModeConfig(
    val modeA: NetworkMode,
    val modeB: NetworkMode,
    val nextModeIsB: Boolean = true
) {
    fun getNextMode(): NetworkMode {
        return if (nextModeIsB) modeB else modeA
    }

    fun getCurrentMode(): NetworkMode {
        return if (nextModeIsB) modeA else modeB
    }

    fun toggle(): ToggleModeConfig {
        return copy(nextModeIsB = !nextModeIsB)
    }
}

sealed class CompatibilityState {
    object Pending : CompatibilityState()
    object Compatible : CompatibilityState()
    data class Incompatible(val reason: String) : CompatibilityState()
    data class PermissionDenied(val method: ControlMethod) : CompatibilityState()
}
