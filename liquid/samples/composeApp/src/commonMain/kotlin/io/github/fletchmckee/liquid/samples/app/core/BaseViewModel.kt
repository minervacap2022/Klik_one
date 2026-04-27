// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class providing common functionality for all ViewModels.
 * Uses coroutines for async operations and StateFlow for state management.
 */
abstract class BaseViewModel<S, E> {
  protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  protected abstract val initialState: S

  private val _state by lazy { MutableStateFlow(initialState) }
  val state: StateFlow<S> by lazy { _state.asStateFlow() }

  protected val currentState: S get() = _state.value

  protected fun updateState(reducer: S.() -> S) {
    _state.value = _state.value.reducer()
  }

  protected fun setState(newState: S) {
    _state.value = newState
  }

  /**
   * Handle one-time events (navigation, snackbars, etc.)
   */
  private val _events = MutableStateFlow<E?>(null)
  val events: StateFlow<E?> = _events.asStateFlow()

  protected fun sendEvent(event: E) {
    _events.value = event
  }

  fun consumeEvent() {
    _events.value = null
  }

  /**
   * Launch a coroutine in viewModelScope
   */
  protected fun launch(block: suspend CoroutineScope.() -> Unit) {
    viewModelScope.launch(block = block)
  }

  /**
   * Clean up resources when ViewModel is no longer needed
   */
  open fun onCleared() {
    viewModelScope.cancel()
  }
}

/**
 * Simple ViewModel without events
 */
abstract class SimpleViewModel<S> : BaseViewModel<S, Nothing>()

/**
 * Remember a ViewModel instance tied to the composable's lifecycle
 */
@Composable
inline fun <reified VM : BaseViewModel<*, *>> rememberViewModel(
  crossinline factory: () -> VM,
): VM = remember { factory() }
