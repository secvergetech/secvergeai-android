package com.secverge.ai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.secverge.ai.data.auth.AuthDependencies
import com.secverge.ai.ui.cockpit.CockpitViewModelFactory
import com.secverge.ai.ui.cockpit.CockpitViewModel
import com.secverge.ai.ui.cockpit.CockpitRepository
import com.secverge.ai.ui.askhachy.AskHachyViewModelFactory
import com.secverge.ai.ui.askhachy.AskHachyViewModel
import com.secverge.ai.ui.askhachy.AskHachyRepository
import com.secverge.ai.data.features.FeatureDependencies
import com.secverge.ai.ui.askhachy.AskHachyScreen
import com.secverge.ai.ui.cockpit.CockpitScreen
import com.secverge.ai.ui.council.AICouncilScreen
import com.secverge.ai.ui.council.AICouncilRepository
import com.secverge.ai.ui.council.AICouncilViewModel
import com.secverge.ai.ui.council.AICouncilViewModelFactory
import com.secverge.ai.ui.delphi.DelphiScreen
import com.secverge.ai.ui.delphi.DelphiViewModelFactory
import com.secverge.ai.ui.delphi.DelphiViewModel
import com.secverge.ai.ui.delphi.DelphiRepository
import com.secverge.ai.ui.home.HomeScreen
import com.secverge.ai.ui.home.HomeViewModel
import com.secverge.ai.ui.home.HomeViewModelFactory
import com.secverge.ai.ui.humancouncil.HumanCouncilScreen
import com.secverge.ai.ui.humancouncil.HumanCouncilRepository
import com.secverge.ai.ui.humancouncil.HumanCouncilViewModel
import com.secverge.ai.ui.humancouncil.HumanCouncilViewModelFactory
import com.secverge.ai.ui.login.LoginScreen
import com.secverge.ai.ui.login.LoginViewModel
import com.secverge.ai.ui.login.LoginViewModelFactory
import com.secverge.ai.ui.shell.AppScaffold
import com.secverge.ai.ui.splash.SplashScreen

@Composable
fun SECVergeNavGraph() {
    val context = LocalContext.current

    val authDependencies = remember {
        AuthDependencies(context.applicationContext)
    }

    val featureDependencies = remember {
        FeatureDependencies(
            secureStore = authDependencies.secureStore
        )
    }

    val navController = rememberNavController()

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val shellRoutes = SECVergeRoute
        .bottomDestinations
        .map { it.route } + SECVergeRoute.Home.route

    if (currentRoute in shellRoutes) {
        AppScaffold(
            currentRoute = currentRoute,
            onDestinationClick = { route ->
                navController.navigate(route.route) {
                    launchSingleTop = true

                    popUpTo(SECVergeRoute.Home.route) {
                        saveState = true
                    }

                    restoreState = true
                }
            }
        ) {
            SECVergeNavHost(
                navController = navController,
                authDependencies = authDependencies,
                featureDependencies = featureDependencies
            )
        }
    } else {
        SECVergeNavHost(
            navController = navController,
            authDependencies = authDependencies,
            featureDependencies = featureDependencies
        )
    }
}

@Composable
private fun SECVergeNavHost(
    navController: NavHostController,
    authDependencies: AuthDependencies,
    featureDependencies: FeatureDependencies
) {
    NavHost(
        navController = navController,
        startDestination = SECVergeRoute.Splash.route
    ) {
        composable(SECVergeRoute.Splash.route) {
            SplashScreen {
                val destination = if (
                    authDependencies.authRepository.isLoggedIn()
                ) {
                    SECVergeRoute.Home.route
                } else {
                    SECVergeRoute.Login.route
                }

                navController.navigate(destination) {
                    popUpTo(SECVergeRoute.Splash.route) {
                        inclusive = true
                    }
                }
            }
        }

        composable(SECVergeRoute.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(
                    authDependencies.authRepository
                )
            )

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(
                        SECVergeRoute.Home.route
                    ) {
                        popUpTo(
                            SECVergeRoute.Login.route
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(SECVergeRoute.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    authDependencies.authRepository
                )
            )

            HomeScreen(
                viewModel = homeViewModel,
                onOpenFeature = { route ->
                    navController.navigate(route.route)
                },
                onLogout = {
                    navController.navigate(
                        SECVergeRoute.Login.route
                    ) {
                        popUpTo(
                            SECVergeRoute.Home.route
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(SECVergeRoute.AskHachy.route) {
            val askHachyViewModel: AskHachyViewModel = viewModel(
                factory = AskHachyViewModelFactory(
                    repository = AskHachyRepository(
                        api = featureDependencies.api
                    )
                )
            )

            AskHachyScreen(
                viewModel = askHachyViewModel
            )
        }

        composable(SECVergeRoute.Cockpit.route) {
            val cockpitViewModel: CockpitViewModel = viewModel(
                factory = CockpitViewModelFactory(
                    repository = CockpitRepository(
                        api = featureDependencies.api
                    )
                )
            )

            CockpitScreen(
                viewModel = cockpitViewModel
            )
        }

        composable(SECVergeRoute.Council.route) {
            val councilViewModel: AICouncilViewModel = viewModel(
                factory = AICouncilViewModelFactory(
                    AICouncilRepository(featureDependencies.api)
                )
            )
            AICouncilScreen(councilViewModel)
        }

        composable(SECVergeRoute.HumanCouncil.route) {
            val humanCouncilViewModel: HumanCouncilViewModel = viewModel(
                factory = HumanCouncilViewModelFactory(
                    HumanCouncilRepository(featureDependencies.api)
                )
            )
            HumanCouncilScreen(humanCouncilViewModel)
        }

        composable(SECVergeRoute.Delphi.route) {
            val delphiViewModel: DelphiViewModel = viewModel(
                factory = DelphiViewModelFactory(
                    DelphiRepository(featureDependencies.api)
                )
            )

            DelphiScreen(
                viewModel = delphiViewModel
            )
        }
    }
}
