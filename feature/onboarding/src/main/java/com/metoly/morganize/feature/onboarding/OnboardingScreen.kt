package com.metoly.morganize.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingPage(
        val icon: ImageVector,
        val title: String,
        val description: String
)

private val pages =
        listOf(
                OnboardingPage(
                        icon = Icons.Default.CreateNewFolder,
                        title = "Organize Your Thoughts",
                        description =
                                "Capture ideas the moment they strike. Morganize keeps everything in one beautiful, searchable place."
                ),
                OnboardingPage(
                        icon = Icons.Default.EditNote,
                        title = "Create & Edit With Ease",
                        description =
                                "Rich text editing that's fast and intuitive. Create notes in seconds and refine them whenever you like."
                ),
                OnboardingPage(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Works on Any Screen",
                        description =
                                "Designed for phones, tablets, and foldables. Your workspace adapts to your device automatically."
                )
        )

/**
 * Onboarding screen shown on first launch. Uses a [HorizontalPager] with animated page indicators.
 *
 * @param onDone Called when the user finishes onboarding; triggers navigation to the List screen.
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    Brush.verticalGradient(
                                            colors =
                                                    listOf(
                                                            MaterialTheme.colorScheme
                                                                    .primaryContainer.copy(
                                                                    alpha = 0.4f
                                                            ),
                                                            MaterialTheme.colorScheme.surface
                                                    )
                                    )
                            )
    ) {
        Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) {
                    page ->
                OnboardingPageContent(page = pages[page])
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Page indicators
            PagerIndicator(pagerState = pagerState, pageCount = pages.size)

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation buttons
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onDone) {
                        Text(text = "Skip", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Button(
                        onClick = {
                            if (isLastPage) {
                                onDone()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    Text(
                            text = if (isLastPage) "Get Started" else "Next",
                            style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(page) { visible = true }

    AnimatedVisibility(
            visible = visible,
            enter =
                    fadeIn() +
                            slideInVertically(
                                    initialOffsetY = { it / 4 },
                                    animationSpec =
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Box(
                    modifier =
                            Modifier.size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PagerIndicator(pagerState: PagerState, pageCount: Int) {
    Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = pagerState.currentPage == index
            val width by
                    animateFloatAsState(
                            targetValue = if (isSelected) 24f else 8f,
                            animationSpec = spring(stiffness = Spring.StiffnessHigh),
                            label = "indicator_width"
                    )
            Box(
                    modifier =
                            Modifier.height(8.dp)
                                    .width(width.dp)
                                    .clip(CircleShape)
                                    .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant
                                    )
            )
        }
    }
}
