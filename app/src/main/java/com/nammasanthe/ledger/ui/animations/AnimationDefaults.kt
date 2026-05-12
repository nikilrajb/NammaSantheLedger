package com.nammasanthe.ledger.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object AnimationDefaults {
    val enterTransition: EnterTransition
        @Composable
        get() = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessLow))

    val exitTransition: ExitTransition
        @Composable
        get() = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                scaleOut(targetScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessLow))

    val slideEnterTransition: EnterTransition
        @Composable
        get() = slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 2 } +
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))

    val slideExitTransition: ExitTransition
        @Composable
        get() = slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 2 } +
                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
}
