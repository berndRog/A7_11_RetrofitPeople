package de.rogallab.mobile.ui.navigation.composables

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.people.PersonViewModel
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.composables.PeopleListScreen
import de.rogallab.mobile.ui.people.composables.PersonScreen
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.NavState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
   // create a NavHostController with a factory function
   navController: NavHostController = rememberNavController(),
   peopleViewModel: PersonViewModel = koinViewModel<PersonViewModel>(), //koinViewModel(),
) {
   val tag = "<-AppNavHost"
   val duration = 700  // in milliseconds

   // N A V I G A T I O N    H O S T -------------------------------------------
   NavHost(
      navController = navController,
      startDestination = NavScreen.PeopleList.route,
      enterTransition = { enterTransition(duration) },
      exitTransition = { exitTransition(duration) },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {
      composable(route = NavScreen.PeopleList.route) {
         PeopleListScreen(
            viewModel = peopleViewModel,
         )
      }
      composable(route = NavScreen.PersonInput.route) {
         PersonScreen(
            viewModel = peopleViewModel,
            validator = koinInject<PersonValidator>(),
            isInputScreen = true,
            id = null
         )
      }
      composable(
         route = NavScreen.PersonDetail.route + "/{personId}",
         arguments = listOf(navArgument("personId") { type = NavType.StringType }),
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("personId")
         PersonScreen(
            viewModel = peopleViewModel,
            validator = koinInject<PersonValidator>(),
            isInputScreen = false,
            id = id
         )
      }
   }

   // O N E   T I M E   E V E N T S   N A V I G A T I O N ---------------------
   // Observing the navigation state and handle navigation
   val navState: NavState
      by peopleViewModel.navStateFlow.collectAsStateWithLifecycle()

   navState.navEvent?.let { navEvent ->
      logVerbose(tag, "navEvent: $navEvent")
      when (navEvent) {
         is NavEvent.NavigateForward -> {
            // Each navigate() pushes the given destination
            // to the top of the stack.
            navController.navigate(navEvent.route)
            // onNavEventHandled() resets the navEvent to null
            peopleViewModel.onNavEventHandled()
         }
         is NavEvent.NavigateReverse -> {
            navController.navigate(navEvent.route) {
               popUpTo(navEvent.route) {  // clears the back stack up to the given route
                  inclusive = true        // ensures that any previous instances of
               }                          // that route are removed
            }
            // onNavEventHandled() resets the navEvent to null
            peopleViewModel.onNavEventHandled()
         }
         is NavEvent.NavigateBack -> {
            navController.popBackStack()
            // onNavEventHandled() resets the navEvent to null
            peopleViewModel.onNavEventHandled()
         }
      } // end of when (it) {
   } // end of navEvent?.let { it: NavEvent ->
}

// A N I M A T I O N S --------------------------------------------------------
private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
   duration: Int
) = fadeIn(
   animationSpec = tween(duration)
) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
   duration: Int
) = fadeOut(
   animationSpec = tween(duration)
) + slideOutOfContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)

private fun popEnterTransition(
   duration: Int
) = scaleIn(
   initialScale = 0.1f,
   animationSpec = tween(duration)
) + fadeIn(animationSpec = tween(duration))

private fun popExitTransition(
   duration: Int
) = scaleOut(
   targetScale = 3.0f,
   animationSpec = tween(duration)
) + fadeOut(animationSpec = tween(duration))
