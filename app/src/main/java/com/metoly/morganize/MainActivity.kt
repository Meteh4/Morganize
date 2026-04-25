package com.metoly.morganize
import androidx.compose.runtime.getValue

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.metoly.morganize.core.ui.theme.MorganizeTheme
import com.metoly.morganize.feature.create.CreateRoute
import com.metoly.morganize.feature.create.CreateScreen
import com.metoly.morganize.feature.create.CreateViewModel
import com.metoly.morganize.feature.edit.EditRoute
import com.metoly.morganize.feature.edit.EditScreen
import com.metoly.morganize.feature.edit.EditViewModel
import com.metoly.morganize.feature.list.ListRoute
import com.metoly.morganize.feature.list.ListScreen
import com.metoly.morganize.feature.list.ListViewModel
import com.metoly.morganize.feature.onboarding.OnboardingRoute
import com.metoly.morganize.feature.onboarding.OnboardingScreen
import com.metoly.morganize.feature.onboarding.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : FragmentActivity() {
    private val onboardingViewModel: OnboardingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            onboardingViewModel.hasCompletedOnboarding.value == null
        }

        enableEdgeToEdge()
        setContent {
            MorganizeTheme {
                MorganizeNavHost(onboardingViewModel = onboardingViewModel)
            }
        }
    }
}

/**
 * Root navigation host using [NavDisplay] from `androidx.navigation3`.
 *
 * The back stack is a [MutableList<Any>] of @Serializable route objects.
 * [rememberViewModelStoreNavEntryDecorator] ensures each NavEntry gets its own independent
 * [ViewModelStore], so ViewModels are properly scoped and cleared when their entry is popped.
 *
 * On first launch, the back stack starts with [OnboardingRoute]. After onboarding is completed, the
 * stack is replaced with [ListRoute].
 */
@Composable
private fun MorganizeNavHost(onboardingViewModel: OnboardingViewModel) {
    val hasCompleted by onboardingViewModel.hasCompletedOnboarding.collectAsState()

    val initialRoute: Any = when (hasCompleted) {
        null -> return
        true -> ListRoute
        false -> OnboardingRoute
    }

    val backStack = remember { mutableStateListOf(initialRoute) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) togetherWith
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
        },
        entryProvider = entryProvider {
            entry<OnboardingRoute> {
                OnboardingScreen(
                    onDone = {
                        onboardingViewModel.completeOnboarding()
                        backStack.clear()
                        backStack.add(ListRoute)
                    }
                )
            }

            entry<ListRoute> {
                val viewModel: ListViewModel = koinViewModel()
                ListScreen(
                    viewModel = viewModel,
                    onCreateNote = { backStack.add(CreateRoute) },
                    onEditNote = { noteId -> backStack.add(EditRoute(noteId)) }
                )
            }

            entry<CreateRoute> {
                val viewModel: CreateViewModel = koinViewModel()
                CreateScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onSaved = { backStack.removeLastOrNull() }
                )
            }

            entry<EditRoute> { route ->
                val viewModel: EditViewModel =
                    koinViewModel(parameters = { parametersOf(route.noteId) })
                EditScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onDone = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
