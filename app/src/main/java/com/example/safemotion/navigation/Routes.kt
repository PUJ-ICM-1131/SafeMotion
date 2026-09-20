package com.example.safemotion.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute : NavKey

@Serializable
data object HomeRoute : NavKey

@Serializable
data object PrepareRunRoute : NavKey

@Serializable
data object ActiveRunRoute : NavKey

@Serializable
data object IncidentAlertRoute : NavKey
