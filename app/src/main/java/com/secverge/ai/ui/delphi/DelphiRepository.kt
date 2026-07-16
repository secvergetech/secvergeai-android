package com.secverge.ai.ui.delphi

/**
 * Delphi currently runs in local Android mode.
 *
 * NavGraph may pass existing API/runtime dependencies into this repository.
 * This constructor accepts them safely while the ViewModel avoids backend calls.
 */
class DelphiRepository(
    private val api: Any? = null,
    vararg ignoredDependencies: Any?
)
