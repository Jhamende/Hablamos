package be.hablamos.app.data

import android.content.Context

data class UserProfile(
    val name: String = "",
    val level: String = "A1",
    val goal: String = "Voyager",
    val dailyMinutes: Int = 10,
    val placementScore: Int = 0,
    val onboardingComplete: Boolean = false
)

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("hablamos_profile", Context.MODE_PRIVATE)

    fun load(): UserProfile = UserProfile(
        name = prefs.getString("name", "") ?: "",
        level = prefs.getString("level", "A1") ?: "A1",
        goal = prefs.getString("goal", "Voyager") ?: "Voyager",
        dailyMinutes = prefs.getInt("dailyMinutes", 10),
        placementScore = prefs.getInt("placementScore", 0),
        onboardingComplete = prefs.getBoolean("onboardingComplete", false)
    )

    fun save(profile: UserProfile) {
        prefs.edit()
            .putString("name", profile.name)
            .putString("level", profile.level)
            .putString("goal", profile.goal)
            .putInt("dailyMinutes", profile.dailyMinutes)
            .putInt("placementScore", profile.placementScore)
            .putBoolean("onboardingComplete", profile.onboardingComplete)
            .apply()
    }

    fun reset() = prefs.edit().clear().apply()
}
