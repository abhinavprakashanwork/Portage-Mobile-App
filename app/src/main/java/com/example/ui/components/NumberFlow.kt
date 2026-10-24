package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PortageFontFamily
import com.example.ui.theme.PortageText

/**
 * Jetpack Compose implementation of smooth rolling number transitions,
 * inspired by `@number-flow/react`.
 *
 * Each digit animates independently with vertical roll transitions (sliding in/out)
 * when the numeric value changes:
 * - When incrementing: digits roll upward.
 * - When decrementing: digits roll downward.
 * - Unchanged digits remain perfectly stationary without layout jitter.
 */
@Composable
fun NumberFlow(
  value: Int,
  modifier: Modifier = Modifier,
  format: String = "%d",
  minDigits: Int = 0,
  digitWidth: Dp = 38.dp,
  textStyle: TextStyle = TextStyle(
    color = PortageText,
    fontSize = 56.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = PortageFontFamily
  )
) {
  var previousValue by remember { mutableIntStateOf(value) }
  val isIncreasing = value >= previousValue

  LaunchedEffect(value) {
    previousValue = value
  }

  val formatted = remember(value, format, minDigits) {
    if (minDigits > 0) {
      String.format("%0${minDigits}d", value)
    } else if (format.contains("%")) {
      String.format(format, value)
    } else {
      value.toString()
    }
  }

  Row(
    modifier = modifier.clipToBounds(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    formatted.forEachIndexed { index, char ->
      Box(
        modifier = Modifier
          .widthIn(min = digitWidth)
          .clipToBounds(),
        contentAlignment = Alignment.Center
      ) {
        AnimatedContent(
          targetState = char,
          transitionSpec = {
            if (isIncreasing) {
              (slideInVertically(
                animationSpec = spring(
                  dampingRatio = 0.78f,
                  stiffness = 380f
                ),
                initialOffsetY = { fullHeight -> fullHeight }
              ) + fadeIn(animationSpec = tween(120, easing = FastOutSlowInEasing)))
                .togetherWith(
                  slideOutVertically(
                    animationSpec = spring(
                      dampingRatio = 0.78f,
                      stiffness = 380f
                    ),
                    targetOffsetY = { fullHeight -> -fullHeight }
                  ) + fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing))
                )
            } else {
              (slideInVertically(
                animationSpec = spring(
                  dampingRatio = 0.78f,
                  stiffness = 380f
                ),
                initialOffsetY = { fullHeight -> -fullHeight }
              ) + fadeIn(animationSpec = tween(120, easing = FastOutSlowInEasing)))
                .togetherWith(
                  slideOutVertically(
                    animationSpec = spring(
                      dampingRatio = 0.78f,
                      stiffness = 380f
                    ),
                    targetOffsetY = { fullHeight -> fullHeight }
                  ) + fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing))
                )
            }
          },
          label = "NumberFlowDigit_${index}"
        ) { targetChar ->
          Text(
            text = targetChar.toString(),
            style = textStyle,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
