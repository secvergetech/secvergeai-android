package com.secverge.ai.navigation

sealed class SECVergeRoute(val route: String, val label: String) {
    data object Splash : SECVergeRoute("splash", "Splash")
    data object Login : SECVergeRoute("login", "Login")
    data object Home : SECVergeRoute("home", "Home")
    data object AskHachy : SECVergeRoute("ask_hachy", "Ask Hachy")
    data object Cockpit : SECVergeRoute("cockpit", "Cockpit")
    data object Council : SECVergeRoute("council", "AI Council")
    data object HumanCouncil : SECVergeRoute("human_council", "Human + AI")
    data object Delphi : SECVergeRoute("delphi", "Delphi")

    companion object {
        val bottomDestinations = listOf(AskHachy, Cockpit, Council, HumanCouncil, Delphi)
    }
}
