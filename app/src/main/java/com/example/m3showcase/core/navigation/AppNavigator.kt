package com.example.m3showcase.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * The only class that changes the back stack. With Navigation 3 the back stack is
 * a plain observable list that we own, so navigating means editing that list.
 */
class AppNavigator(private val backStack: NavBackStack<NavKey>) {

    val current: NavKey? get() = backStack.lastOrNull()

    fun navigate(destination: Destination) {
        if (backStack.lastOrNull() != destination) backStack.add(destination)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    /** Bottom bar: Home always stays at the root, so Back from a tab goes Home. */
    fun selectTopLevel(destination: Destination) {
        if (backStack.lastOrNull() == destination) return
        replaceAll(if (destination == Destination.Home) listOf(Destination.Home) else listOf(Destination.Home, destination))
    }

    fun popToHome() = replaceAll(listOf(Destination.Home))

    fun replaceAll(destinations: List<Destination>) {
        backStack.clear()
        backStack.addAll(destinations)
    }
}
