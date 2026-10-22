package dev.artplus.mobile.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.basic.TopAppBarState
import io.github.suqi8.coui.kmp.basic.rememberTopAppBarState

/**
 * A [ScrollBehavior] that keeps the app bar and the list under the same finger.
 *
 * COUI's `COUIScrollBehavior` is an exit-until-collapsed behavior: it collapses the bar inside
 * `onPreScroll`, so the list only starts moving once the large title has finished shrinking. This
 * one never intercepts the pre-scroll — the bar's height offset follows the delta the list actually
 * consumed, so the title shrinks at the same rate the content moves.
 *
 * @param state The [TopAppBarState] shared with the bar.
 * @param snapAnimationSpec The animation that settles the bar after a fling.
 */
@Composable
internal fun rememberLinkedTopAppBarScrollBehavior(
    state: TopAppBarState = rememberTopAppBarState(),
    snapAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 200),
): ScrollBehavior = remember(state, snapAnimationSpec) {
    LinkedTopAppBarScrollBehavior(state, snapAnimationSpec)
}

private class LinkedTopAppBarScrollBehavior(
    override val state: TopAppBarState,
    override val snapAnimationSpec: AnimationSpec<Float>,
) : ScrollBehavior {
    override val isPinned: Boolean = false
    override val flingAnimationSpec: DecayAnimationSpec<Float>? = null

    /** Whether the current gesture pulled the list past its top, i.e. the bar may expand. */
    private var atTopPull: Boolean = false

    override val nestedScrollConnection: NestedScrollConnection =
        object : NestedScrollConnection {
            // Never swallow the scroll before the list sees it: that is what made the bar collapse
            // first and the content wait.
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                Offset.Zero

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // Collapse with the list, but only expand once the list is pinned at its top:
                // `available` is what the list could not consume, which is the pull-down overscroll.
                val delta = when {
                    consumed.y < 0f -> {
                        atTopPull = false
                        consumed.y
                    }
                    consumed.y == 0f && available.y > 0f -> {
                        atTopPull = true
                        available.y
                    }
                    else -> 0f
                }
                if (delta == 0f) return Offset.Zero
                val previous = state.heightOffset
                state.heightOffset += delta
                state.contentOffset += delta
                return Offset(0f, state.heightOffset - previous)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val limit = state.heightOffsetLimit
                val current = state.heightOffset
                if (limit != 0f && current != 0f && current != limit) {
                    // A half-collapsed bar settles back to collapsed unless the pull happened at
                    // the list's top; expanding mid-list is what felt like a premature reveal.
                    val target = if (atTopPull) 0f else limit
                    AnimationState(initialValue = current).animateTo(target, snapAnimationSpec) {
                        state.heightOffset = value
                    }
                }
                return Velocity.Zero
            }
        }
}
