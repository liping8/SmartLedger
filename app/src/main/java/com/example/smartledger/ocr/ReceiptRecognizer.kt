package com.example.smartledger.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrResult(
    val amount: Double?,
    val categoryGuess: String,
    val rawText: String
)

object ReceiptRecognizer {

    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    // Category keywords mapping
    private val categoryKeywords = mapOf(
        "餐饮" to listOf("餐", "饭", "食", "奶茶", "咖啡", "外卖", "美团", "饿了么", "肯德基", "麦当劳", "火锅", "面", "饮", "茶"),
        "交通" to listOf("打车", "滴滴", "出租", "地铁", "公交", "加油", "停车", "高铁", "火车", "机票", "航空"),
        "购物" to listOf("超市", "商场", "淘宝", "京东", "天猫", "拼多多", "服装", "衣", "鞋"),
        "住房" to listOf("房租", "水电", "物业", "燃气", "暖气"),
        "娱乐" to listOf("电影", "游戏", "KTV", "旅游", "门票", "演出"),
        "医疗" to listOf("医院", "药", "诊所", "挂号", "体检"),
        "教育" to listOf("学费", "书", "课程", "培训", "教材"),
        "通讯" to listOf("话费", "流量", "宽带", "移动", "联通", "电信"),
    )

    suspend fun recognize(bitmap: Bitmap): OcrResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        val text = suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result -> cont.resume(result.text) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }

        val amount = extractAmount(text)
        val category = guessCategory(text)

        return OcrResult(amount = amount, categoryGuess = category, rawText = text)
    }

    private fun extractAmount(text: String): Double? {
        // Match patterns like: ¥123.45, 合计 123.45, 总计:123.45, 实付 45.00
        val patterns = listOf(
            Regex("""[¥￥]\s*(\d+\.?\d*)"""),
            Regex("""(?:合计|总计|实付|实收|应付|总额|金额)[：:￥¥\s]*(\d+\.?\d*)"""),
            Regex("""(\d+\.\d{2})"""),
        )

        for (pattern in patterns) {
            val matches = pattern.findAll(text).toList()
            if (matches.isNotEmpty()) {
                // Return the largest amount found (likely the total)
                return matches.mapNotNull { it.groupValues[1].toDoubleOrNull() }.maxOrNull()
            }
        }
        return null
    }

    private fun guessCategory(text: String): String {
        val lowerText = text.lowercase()
        var bestCategory = "其他"
        var bestScore = 0

        for ((category, keywords) in categoryKeywords) {
            val score = keywords.count { lowerText.contains(it) }
            if (score > bestScore) {
                bestScore = score
                bestCategory = category
            }
        }
        return bestCategory
    }
}
