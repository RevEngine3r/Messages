package org.fossify.messages.helpers

// Latin keywords get classic word boundaries, \p{L} makes them work for Unicode words too
private const val LATIN_OTP_KEYWORDS =
    "one[-\\s]?time|otp|otponetime|verification|verified|verify|validation|validate|security|authentication|authenticate|access|confirm(?:ation)?|passcode|password|pin|code|ticket|token"

// Persian / Arabic equivalents, written with \u escapes so they stay readable in source:
// کد / کد / كود (code), تایید / تأیید / تاييد / تصدیق (confirmation), رمز / رمز عبور (passcode),
// شماره یکبار / شماره یکبارمصرف (one-time number), امنیت (security), دسترسی (access),
// فعال‌سازی (activation), تحقق (verification), تفعيل (activation), مصادقة (authentication),
// حریمیه (attachment), وصله (attachment)
private const val PERSIAN_ARABIC_OTP_KEYWORDS =
    "\u06a9\u062f" +                                    // کد
        "|\u0643\u062f" +                                    // كد
        "|\u0643\u0648\u062f" +                              // كود
        "|\u062a\u0627[\u064a\u06cc]{1,2}\u062f" +                       // تایید / تاييد / تايد
        "|\u062a[\u0623\u0621\u0626][\u064a\u06cc]{1,2}\u062f" +           // تأیید / تأيد
        "|\u062a\u0635\u062f[\u064a\u06cc]\u0642[\u0643\u06a9]?" +      // تصدیق / تصدیقك / تصدیقک
        "|\u0631\u0645\u0632(?:\\s*\u0639\u0628\u0648\u0631)?" +  // رمز / رمز عبور / رمزعبور
        "|\u0634\u0645\u0627?\u0631\u0647(?:\\s*\\u200c?\\s*[\u064a\u06cc]\u06a9(?:\\s*\\u200c?\\s*\u0628\u0627?\u0631(?:\\s*\u0645\u0635?\u0631\u0641)?)?)?" + // شماره یکبار(مصرف)
        "|\u062d\u0631[\u064a\u06cc]\u0645[\u064a\u06cc]\u0647" +          // حریمیه
        "|\u0648\u0635\u0644\u0647" +                        // وصله
        "|\u0627\u0645\u0646[\u064a\u06cc]\u062a" +                  // امنیت
        "|\u062f\u0633\u062a\u0631\u0633[\u064a\u06cc]" +              // دسترسی
        "|\u0641\u0639\u0627\u0644\\u200c?\u0633\u0627\u0632[\u064a\u06cc]" +   // فعال‌سازی / فعالسازی
        "|\u062a\u062d\u0642\u0642" +                        // تحقق
        "|\u062a\u0641\u0639\u064a\u0644" +                  // تفعيل
        "|\u0645\u0635\u0627\u062f\u0642\u0629"               // مصادقة

private val OTP_KEYWORD_REGEX = Regex(
    "(?<!\\p{L})(?:$LATIN_OTP_KEYWORDS)(?!\\p{L})" +
        "|(?<!\\p{L})(?:$PERSIAN_ARABIC_OTP_KEYWORDS)(?!\\p{L})",
    RegexOption.IGNORE_CASE,
)

// maximal runs of digits, so that nothing from a longer sequence (phone number, long ID) is picked up
private val DIGIT_RUN_REGEX = Regex("(?<!\\d)\\d+(?!\\d)")

// alphanumeric formats like "A1B2-C3D4-E5F6" or a single "X7Y9Z2" block
private val ALPHANUMERIC_CODE_REGEX = Regex("(?<![A-Z0-9])[A-Z0-9]{4,8}(?:-[A-Z0-9]{3,8}){1,2}(?![A-Z0-9])")

// Persian (۰-۹) and Arabic-Indic (٠-٩) digits mapped to their ASCII equivalents, so codes
// written with those digits are detected just like English ones
private fun String.normalizeOtpDigits(): String {
    return map {
        when {
            it in '\u06f0'..'\u06f9' -> '0' + (it - '\u06f0')
            it in '\u0660'..'\u0669' -> '0' + (it - '\u0660')
            else -> it
        }
    }.joinToString("")
}

/**
 * Tries to extract a one-time-password / verification code from a message body.
 * Supports English as well as Persian / Arabic wording, and Latin, Persian or Arabic-Indic digits.
 * Returns the code as written in the original message, or null if the message doesn't look like an OTP message.
 */
fun extractOtpCode(body: String): String? {
    if (body.isBlank()) {
        return null
    }

    val normalizedBody = body.normalizeOtpDigits()
    if (!OTP_KEYWORD_REGEX.containsMatchIn(normalizedBody)) {
        return null
    }

    // typical codes are 4-8 digits; skip longer sequences (phone numbers, IDs) and date parts such
    // as "1405/07/23" or "1405-07-23", whose pieces are glued to a / or - separator
    val numericCandidate = DIGIT_RUN_REGEX.findAll(normalizedBody)
        .filter { it.value.length in 4..8 }
        .filter { match ->
            val previous = normalizedBody.getOrNull(match.range.first - 1)
            val next = normalizedBody.getOrNull(match.range.last + 1)
            previous != '/' && previous != '-' && next != '/' && next != '-'
        }
        .map { it.range.first to it.value }
        .firstOrNull { (_, code) -> code.any { ch -> ch != '0' } }
    if (numericCandidate != null) {
        // return the code as written in the original message (Persian / Arabic-Indic digits kept)
        return body.substring(numericCandidate.first until numericCandidate.first + numericCandidate.second.length)
    }

    val alphanumericMatch = ALPHANUMERIC_CODE_REGEX.find(normalizedBody.uppercase())
    return alphanumericMatch?.takeIf { it.value.any { ch -> ch.isDigit() } }?.let { match ->
        body.substring(match.range.first until match.range.first + match.value.length)
    }
}
