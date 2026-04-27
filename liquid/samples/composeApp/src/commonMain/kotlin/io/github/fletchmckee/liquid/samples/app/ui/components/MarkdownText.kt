// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A composable that renders markdown-formatted text.
 * Supports: **bold**, *italic*, `inline code`, ~~strikethrough~~,
 * headers (#, ##, ###), bullet points (- or *), and numbered lists.
 */
@Composable
fun MarkdownText(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.bodyMedium,
  color: Color = Color.Unspecified,
  selectable: Boolean = false,
) {
  val lines = remember(text) { text.split("\n") }
  val content = @Composable {
    Column(modifier = modifier) {
      lines.forEachIndexed { index, line ->
        MarkdownLine(
          line = line,
          style = style,
          color = color,
        )
        if (index < lines.lastIndex) {
          Spacer(Modifier.height(4.dp))
        }
      }
    }
  }

  if (selectable) {
    SelectionContainer { content() }
  } else {
    content()
  }
}

@Composable
private fun MarkdownLine(
  line: String,
  style: TextStyle,
  color: Color,
) {
  val trimmed = line.trim()

  when {
    // Headers
    trimmed.startsWith("### ") -> {
      Text(
        text = parseInlineMarkdown(trimmed.removePrefix("### ")),
        style = style.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = style.fontSize * 1.1f,
        ),
        color = color,
      )
    }

    trimmed.startsWith("## ") -> {
      Text(
        text = parseInlineMarkdown(trimmed.removePrefix("## ")),
        style = style.copy(
          fontWeight = FontWeight.Bold,
          fontSize = style.fontSize * 1.2f,
        ),
        color = color,
      )
    }

    trimmed.startsWith("# ") -> {
      Text(
        text = parseInlineMarkdown(trimmed.removePrefix("# ")),
        style = style.copy(
          fontWeight = FontWeight.Bold,
          fontSize = style.fontSize * 1.4f,
        ),
        color = color,
      )
    }

    // Bullet points (- or *)
    trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
      Row {
        Text(
          text = "•",
          style = style,
          color = color,
          modifier = Modifier.padding(end = 8.dp),
        )
        Text(
          text = parseInlineMarkdown(trimmed.drop(2)),
          style = style,
          color = color,
        )
      }
    }

    // Numbered lists (1. 2. 3. etc)
    trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
      val numberEnd = trimmed.indexOf(". ")
      val number = trimmed.substring(0, numberEnd + 1)
      val content = trimmed.substring(numberEnd + 2)
      Row {
        Text(
          text = number,
          style = style,
          color = color,
          modifier = Modifier.width(24.dp),
        )
        Text(
          text = parseInlineMarkdown(content),
          style = style,
          color = color,
        )
      }
    }

    // Regular text
    else -> {
      Text(
        text = parseInlineMarkdown(line),
        style = style,
        color = color,
      )
    }
  }
}

/**
 * Parses inline markdown formatting (bold, italic, code, strikethrough)
 * and returns an AnnotatedString with appropriate styling.
 */
private fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
  var i = 0
  while (i < text.length) {
    when {
      // Bold + Italic (***text***)
      text.startsWith("***", i) -> {
        val end = text.indexOf("***", i + 3)
        if (end != -1) {
          withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
            append(text.substring(i + 3, end))
          }
          i = end + 3
        } else {
          append(text[i])
          i++
        }
      }

      // Bold (**text**)
      text.startsWith("**", i) -> {
        val end = text.indexOf("**", i + 2)
        if (end != -1) {
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(i + 2, end))
          }
          i = end + 2
        } else {
          append(text[i])
          i++
        }
      }

      // Italic (*text* or _text_)
      (text.startsWith("*", i) && !text.startsWith("**", i)) ||
        (text.startsWith("_", i) && !text.startsWith("__", i)) -> {
        val delimiter = text[i].toString()
        val end = text.indexOf(delimiter, i + 1)
        if (end != -1 && end > i + 1) {
          withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            append(text.substring(i + 1, end))
          }
          i = end + 1
        } else {
          append(text[i])
          i++
        }
      }

      // Strikethrough (~~text~~)
      text.startsWith("~~", i) -> {
        val end = text.indexOf("~~", i + 2)
        if (end != -1) {
          withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
            append(text.substring(i + 2, end))
          }
          i = end + 2
        } else {
          append(text[i])
          i++
        }
      }

      // Inline code (`code`)
      text.startsWith("`", i) -> {
        val end = text.indexOf("`", i + 1)
        if (end != -1) {
          withStyle(
            SpanStyle(
              fontFamily = FontFamily.Monospace,
              background = Color.Black.copy(alpha = 0.08f),
              fontSize = 14.sp,
            ),
          ) {
            append(" ")
            append(text.substring(i + 1, end))
            append(" ")
          }
          i = end + 1
        } else {
          append(text[i])
          i++
        }
      }

      // Regular character
      else -> {
        append(text[i])
        i++
      }
    }
  }
}
