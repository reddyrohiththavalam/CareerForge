/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kotlin.math.ceil

/**
 * BionicText: A robust prefix-bolding typography filter component optimizing reading
 * and comprehension speed for neurodivergent or burnt-out students.
 *
 * Bolds the first 40-50% prefix of every word, anchoring the eye's saccadic fixation
 * point to accelerate comprehension while reducing cognitive fatigue.
 */
@Composable
fun BionicText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    boldWeight: FontWeight = FontWeight.ExtraBold,
    normalWeight: FontWeight = FontWeight.Normal,
    boldRatio: Float = 0.45f,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE
) {
    val annotatedString = remember(text, boldRatio, boldWeight, normalWeight) {
        formatBionicString(text, boldRatio, boldWeight, normalWeight)
    }

    Text(
        text = annotatedString,
        modifier = modifier.testTag("bionic_text"),
        style = style,
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines
    )
}

/**
 * Parses raw text and applies Bionic Reading SpanStyles to word prefixes.
 */
fun formatBionicString(
    text: String,
    boldRatio: Float = 0.45f,
    boldWeight: FontWeight = FontWeight.ExtraBold,
    normalWeight: FontWeight = FontWeight.Normal
): AnnotatedString {
    return buildAnnotatedString {
        if (text.isEmpty()) return@buildAnnotatedString

        var currentIndex = 0
        val wordRegex = Regex("""\b(\w+)\b""")
        val matches = wordRegex.findAll(text)

        for (match in matches) {
            val range = match.range
            // Append any non-word characters before this word (spaces, punctuation, newlines)
            if (range.first > currentIndex) {
                append(text.substring(currentIndex, range.first))
            }

            val word = match.value
            val boldLength = when {
                word.length <= 3 -> 1
                word.length <= 5 -> 2
                else -> ceil(word.length * boldRatio).toInt().coerceAtLeast(1)
            }

            // Bold prefix
            pushStyle(SpanStyle(fontWeight = boldWeight))
            append(word.substring(0, boldLength))
            pop()

            // Normal suffix
            if (boldLength < word.length) {
                pushStyle(SpanStyle(fontWeight = normalWeight))
                append(word.substring(boldLength))
                pop()
            }

            currentIndex = range.last + 1
        }

        // Append any trailing characters
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
