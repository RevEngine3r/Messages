package org.fossify.messages.helpers

private val OTP_KEYWORD_REGEX = Regex(
    "\\b(one[-\\s]?time|otp|otponetime|verification|verified|verify|validation|validate|security|authentication|authenticate|access|confirm(?:ation)?|passcode|password|pin|code|ticket|token)\\b",
    RegexOption.IGNORE_CASE,
)

// typical numeric codes: 4-8 consecutive digits
private val NUMERIC_CODE_REGEX = Regex("\\b\\d{4,8}\\b")

// alphanumeric formats like "A1B2-C3D4-E5F6" or a single "X7Y9Z2" block
private val ALPHANUMERIC_CODE_REGEX = Regex("\\b[A-Z0-9]{4,8}(?:-[A-Z0-9]{3,8}){1,2}\\b")

/**
 * Tries to extract a one-time-password / verification code from a message body.
 * Returns null if the message doesn't look like an OTP message.
 */
fun extractOtpCode(body: String): String? {
    if (body.isBlank() || !OTP_KEYWORD_REGEX.containsMatchIn(body)) {
        return null
    }

    // avoid picking up years, phone numbers or long digit sequences that are part of longer ones
    val numericCandidate = NUMERIC_CODE_REGEX.findAll(body)
        .map { it.value }
        .firstOrNull { !it.all { ch -> ch == '0' } }
    if (numericCandidate != null) {
        return numericCandidate
    }

    val alphanumericCandidate =
        ALPHANUMERIC_CODE_REGEX.find(body.uppercase())?.value?.takeIf { it.any { ch -> ch.isDigit() } }
    return alphanumericCandidate
}
