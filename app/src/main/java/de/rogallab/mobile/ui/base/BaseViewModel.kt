package de.rogallab.mobile.ui.base

import androidx.lifecycle.ViewModel
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorState
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

open class BaseViewModel(
   private val _exceptionHandler: CoroutineExceptionHandler,
   private val _tag: String = "<-BaseViewModel"
): ViewModel(),
   IErrorHandler,
   INavigationHandler {

   private val _coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
   override fun onCleared() {
      super.onCleared()
      _coroutineScope.coroutineContext.cancel()
   }

   // =========================================================================
   // ErrorHandling
   // =========================================================================
   private val _errorStateFlow: MutableStateFlow<ErrorState> =
      MutableStateFlow(ErrorState())
   override val errorStateFlow: StateFlow<ErrorState> =
      _errorStateFlow.asStateFlow()

   override fun handleErrorEvent(t: Throwable) {
      onErrorEvent(ErrorParams(throwable = t, navEvent = null))
   }
   // save the previous event
   private var savedParams: ErrorParams? = null

   override fun onErrorEvent(params: ErrorParams) {
      if (params == savedParams) return

      logDebug(_tag, "onErrorEvent()")
      savedParams = params

      params.throwable?.let {
         var error = "$it.localizedMessage"
         when (it) {
            is CancellationException -> error = "Cancellation error: $error"
            //   is RedirectResponseException -> error = "Redirect error: $error"
            //   is ClientRequestException -> error = "Client error: $error"
            //   is ServerResponseException -> error = "Server error: $error"
            //   is ConnectTimeoutException -> error = "Connection time out: $error"
            //   is SocketTimeoutException -> error = "Socket time out: $error"
            //   is UnknownHostException -> error = "no internet connection: $error"
         }
         logError(_tag, error)
         savedParams = params.copy(message = error)
      }

      // update the errorStateFlow with savedParams
      _errorStateFlow.update { it: ErrorState ->
         it.copy(params = savedParams)
      }
   }

   override fun onErrorEventHandled() {
      _coroutineScope.launch(_exceptionHandler) {
         delay(100)
         logDebug(_tag, "onErrorEventHandled()")
         savedParams = null
         _errorStateFlow.update { it: ErrorState ->
            it.copy(params = savedParams)
         }
      }
   }

   // =========================================================================
   // Navigation Handling
   // =========================================================================
   // StateFlow to observe navigation state
   private val _navStateFlow: MutableStateFlow<NavState> =
      MutableStateFlow(NavState())
   override val navStateFlow: StateFlow<NavState> =
      _navStateFlow.asStateFlow()

   // save the previous navigation event
   private var savedNavEvent: NavEvent? = null

   // navigate to the event
   override fun onNavigate(navEvent: NavEvent) {
      if (navEvent == savedNavEvent) return
      logVerbose(_tag, "onNavigate() event:${navEvent}")
      savedNavEvent = navEvent
      _navStateFlow.update { it: NavState ->
         it.copy(navEvent = navEvent)
      }
   }

   // delete the last navigation event when handled
   override fun onNavEventHandled() {
      _coroutineScope.launch(_exceptionHandler) {
         delay(100) // Delay to ensure navigation has been processed
         logVerbose(_tag, "onNavEventHandled()")
         _navStateFlow.update { it: NavState ->
            it.copy(navEvent = null)
         }
         savedNavEvent = null
      }
   }
}