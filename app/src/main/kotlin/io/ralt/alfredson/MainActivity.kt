package io.ralt.alfredson

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.ralt.alfredson.data.UserPrefs
import io.ralt.alfredson.i18n.LocaleManager
import io.ralt.alfredson.ui.calendar.CalendarScreen
import io.ralt.alfredson.ui.calendar.DayDetailScreen
import io.ralt.alfredson.ui.onboarding.OnboardingScreen
import io.ralt.alfredson.ui.settings.SettingsScreen
import io.ralt.alfredson.ui.theme.AlfredsonTheme
import io.ralt.alfredson.ui.today.TodayScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlfredsonTheme {
                RootNav()
            }
        }
    }
}

object Routes {
    const val ONBOARDING = "onboarding"
    const val TODAY = "today"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val DAY_DETAIL = "day/{dayIndex}"
    fun dayDetail(dayIndex: Int) = "day/$dayIndex"
}

@Composable
private fun RootNav() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as AlfredsonApp
    val prefs by app.userPrefs.flow.collectAsStateWithLifecycle(initialValue = UserPrefs())

    LaunchedEffect(prefs.language) {
        LocaleManager.apply(prefs.language)
    }

    val nav = rememberNavController()
    val start = if (prefs.onboarded) Routes.TODAY else Routes.ONBOARDING

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = {
                nav.navigate(Routes.TODAY) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.TODAY) {
            TodayScreen(
                onOpenCalendar = { nav.navigate(Routes.CALENDAR) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.CALENDAR) {
            CalendarScreen(
                onBack = { nav.popBackStack() },
                onOpenDay = { nav.navigate(Routes.dayDetail(it)) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(
            Routes.DAY_DETAIL,
            arguments = listOf(navArgument("dayIndex") { type = NavType.IntType }),
        ) { entry ->
            val dayIndex = entry.arguments?.getInt("dayIndex") ?: 0
            DayDetailScreen(
                dayIndex = dayIndex,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
