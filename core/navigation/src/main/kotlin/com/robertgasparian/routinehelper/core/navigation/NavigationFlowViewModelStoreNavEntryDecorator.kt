package com.robertgasparian.routinehelper.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

/**
 * Creates entry-local ViewModel stores and optionally exposes a parent entry's store.
 *
 * A child opts into a flow with [NavigationFlowScope.parent]. The referenced parent entry must
 * remain on the same back stack for the complete flow. Popping that parent clears the shared store
 * and every ViewModel scoped to it.
 */
@Composable
fun <T : Any> rememberNavigationFlowViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
): NavigationFlowViewModelStoreNavEntryDecorator<T> {
    val viewModelStoreProvider = rememberViewModelStoreProvider(viewModelStoreOwner)
    return remember(viewModelStoreOwner) {
        NavigationFlowViewModelStoreNavEntryDecorator(viewModelStoreProvider)
    }
}

/**
 * Supplies every entry with its local owner and flow children with their parent's owner.
 *
 * Keep the saveable-state decorator before this decorator so entry and flow ViewModels can use
 * SavedStateHandle.
 */
class NavigationFlowViewModelStoreNavEntryDecorator<T : Any>(
    viewModelStoreProvider: ViewModelStoreProvider,
) : NavEntryDecorator<T>(
    onPop = viewModelStoreProvider::clearKey,
    decorate = { entry ->
        val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
        val localOwner = rememberViewModelStoreOwner(
            key = entry.contentKey,
            provider = viewModelStoreProvider,
            savedStateRegistryOwner = savedStateRegistryOwner,
        )
        val providedValues = mutableListOf<ProvidedValue<*>>(
            LocalViewModelStoreOwner provides localOwner,
        )

        entry.metadata[NavigationFlowScope.ParentContentKey]?.let { parentContentKey ->
            val parentOwner = rememberViewModelStoreOwner(
                key = parentContentKey,
                provider = viewModelStoreProvider,
                savedStateRegistryOwner = savedStateRegistryOwner,
            )
            providedValues += LocalNavigationFlowViewModelStoreOwner provides parentOwner
        }

        CompositionLocalProvider(values = providedValues.toTypedArray()) {
            entry.Content()
        }
    },
)

/**
 * Metadata contract used by a child entry to join a parent entry's navigation flow.
 */
object NavigationFlowScope {
    fun parent(parentContentKey: Any): Map<String, Any> = metadata {
        put(ParentContentKey, parentContentKey)
    }

    object ParentContentKey : NavMetadataKey<Any>
}

/**
 * The parent flow's ViewModel owner. It is available only inside entries declaring a parent.
 */
val LocalNavigationFlowViewModelStoreOwner = staticCompositionLocalOf<ViewModelStoreOwner> {
    error("No navigation flow ViewModelStoreOwner was provided for this entry")
}
