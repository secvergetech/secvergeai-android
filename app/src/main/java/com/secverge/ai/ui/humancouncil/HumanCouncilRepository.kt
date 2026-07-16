package com.secverge.ai.ui.humancouncil

/**
 * Human Decision Room currently runs in local Android mode.
 *
 * NavGraph may still pass existing network/API dependencies into this repository.
 * This constructor intentionally accepts those dependencies so navigation wiring
 * stays compatible while HumanCouncilViewModel avoids backend calls.
 */
class HumanCouncilRepository(
    vararg ignoredDependencies: Any?
)
