package com.example.utils

object ScoringEngine {

    fun calculateScore(
        isSoftwareOk: Boolean,
        isLayarOk: Boolean,
        isKameraOk: Boolean,
        isSpeakerOk: Boolean,
        isMicOk: Boolean,
        isSensorOk: Boolean,
        isBateraiOk: Boolean,
        isConnectivityOk: Boolean
    ): ScoreResult {
        var score = 0
        
        // 1. Software normal: 20 points
        if (isSoftwareOk) {
            score += 20
        }
        
        // 2. Layar normal: 20 points
        if (isLayarOk) {
            score += 20
        }
        
        // 3. Kamera normal: 15 points
        if (isKameraOk) {
            score += 15
        }
        
        // 4. Speaker & mic normal: 15 points (Speaker = 8, Mic = 7)
        var speakerAndMicScore = 0
        if (isSpeakerOk) speakerAndMicScore += 8
        if (isMicOk) speakerAndMicScore += 7
        score += speakerAndMicScore
        
        // 5. Sensor normal: 10 points
        if (isSensorOk) {
            score += 10
        }
        
        // 6. Baterai normal: 10 points
        if (isBateraiOk) {
            score += 10
        }
        
        // 7. Konektivitas normal: 10 points
        if (isConnectivityOk) {
            score += 10
        }
        
        val category = when (score) {
            in 85..100 -> "Sangat Layak"
            in 70..84 -> "Layak dengan catatan"
            in 50..69 -> "Perlu nego / perlu servis"
            else -> "Tidak direkomendasikan"
        }

        return ScoreResult(score, category)
    }
}

data class ScoreResult(
    val score: Int,
    val recommendation: String
)
