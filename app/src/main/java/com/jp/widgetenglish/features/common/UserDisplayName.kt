package com.jp.widgetenglish.features.common

import java.util.Locale

const val USER_DISPLAY_NAME_MIN_LENGTH = 3
const val USER_DISPLAY_NAME_MAX_LENGTH = 25

private val GENERIC_NAMES = setOf("usuario", "user")

fun resolveUserDisplayName(
    localName: String? = null,
    firebaseDisplayName: String? = null,
    email: String? = null
): String {
    return localName.asSpecificName()
        ?: firebaseDisplayName.asSpecificName()
        ?: email.displayNameFromEmail()
        ?: ""
}

fun firstDisplayNameOrBlank(name: String?): String {
    return name.asSpecificName()
        ?.split(Regex("\\s+"))
        ?.firstOrNull()
        .orEmpty()
}

private fun String?.asSpecificName(): String? {
    val cleaned = this
        ?.trim()
        ?.replace(Regex("\\s+"), " ")
        ?.takeIf { it.isNotBlank() }
        ?: return null

    return cleaned.takeUnless { it.lowercase(Locale.getDefault()) in GENERIC_NAMES }
}

private fun String?.displayNameFromEmail(): String? {
    val localPart = this
        ?.trim()
        ?.substringBefore("@", missingDelimiterValue = "")
        ?.takeIf { it.isNotBlank() }
        ?: return null

    val cleaned = localPart
        .replace(Regex("[._-]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .takeIf { it.any(Char::isLetterOrDigit) }
        ?: return null

    return cleaned
        .split(" ")
        .joinToString(" ") { token ->
            token.lowercase(Locale.getDefault())
                .replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
        }
}
