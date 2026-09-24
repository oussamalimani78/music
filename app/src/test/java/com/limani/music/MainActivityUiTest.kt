package com.limani.music

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.limani.music.data.model.Song
import com.limani.music.player.PlayerController
import com.limani.music.ui.dialog.NowPlayingBottomSheetDialogFragment
import com.limani.music.ui.fragment.FavoritesFragment
import com.limani.music.ui.fragment.HomeFragment
import com.limani.music.ui.fragment.LibraryFragment
import com.limani.music.ui.fragment.SearchFragment
import com.limani.music.ui.fragment.SettingsFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityUiTest {

    @Test
    fun testActivityLaunchAndDefaultHomeFragment() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
                val bottomNav: BottomNavigationView = activity.findViewById(R.id.bottomNavigation)
                assertNotNull(bottomNav)
                assertEquals(R.id.navigation_home, bottomNav.selectedItemId)

                val currentFragment: Fragment? = activity.supportFragmentManager.findFragmentByTag("home")
                assertNotNull(currentFragment)
                assertTrue(currentFragment is HomeFragment)
            }
        }
    }

    @Test
    fun testBottomNavigationTabs() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
                val bottomNav: BottomNavigationView = activity.findViewById(R.id.bottomNavigation)

                // Navigate to Library
                bottomNav.selectedItemId = R.id.navigation_library
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
                var currentFragment = activity.supportFragmentManager.findFragmentByTag("library")
                assertNotNull(currentFragment)
                assertTrue(currentFragment is LibraryFragment)

                // Navigate to Search
                bottomNav.selectedItemId = R.id.navigation_search
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
                currentFragment = activity.supportFragmentManager.findFragmentByTag("search")
                assertNotNull(currentFragment)
                assertTrue(currentFragment is SearchFragment)

                // Navigate to Favorites
                bottomNav.selectedItemId = R.id.navigation_favorites
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
                currentFragment = activity.supportFragmentManager.findFragmentByTag("favorites")
                assertNotNull(currentFragment)
                assertTrue(currentFragment is FavoritesFragment)

                // Navigate to Settings
                bottomNav.selectedItemId = R.id.navigation_settings
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
                currentFragment = activity.supportFragmentManager.findFragmentByTag("settings")
                assertNotNull(currentFragment)
                assertTrue(currentFragment is SettingsFragment)
            }
        }
    }

    @Test
    fun testMiniPlayerVisibilityAndInteraction() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

                val miniCard: View = activity.findViewById(R.id.miniPlayerCard)
                val miniTitle: TextView = activity.findViewById(R.id.miniSongTitle)
                val miniArtist: TextView = activity.findViewById(R.id.miniSongArtist)
                val miniPlayPauseBtn: ImageButton = activity.findViewById(R.id.miniPlayPauseBtn)

                // Initially no song playing
                assertEquals(View.GONE, miniCard.visibility)

                // Simulate playing a song using PlayerController
                val playerController = (activity.application as? MusicApplication)?.playerController
                    ?: PlayerController(activity.applicationContext)

                val sampleSong = Song("s_test", "Test Title", "Test Artist", "Test Album", 180000L, 0)
                playerController.playSong(sampleSong)

                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

                assertEquals(View.VISIBLE, miniCard.visibility)
                assertEquals("Test Title", miniTitle.text.toString())
                assertTrue(miniArtist.text.toString().contains("Test Artist"))

                // Test toggle play/pause click
                miniPlayPauseBtn.performClick()
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
            }
        }
    }

    @Test
    fun testNowPlayingBottomSheetOpening() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

                val playerController = (activity.application as? MusicApplication)?.playerController
                    ?: PlayerController(activity.applicationContext)

                val sampleSong = Song("s_test2", "Title Two", "Artist Two", "Album Two", 200000L, 0)
                playerController.playSong(sampleSong)

                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

                val miniCard: View = activity.findViewById(R.id.miniPlayerCard)
                miniCard.performClick()

                ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

                val dialog = activity.supportFragmentManager.findFragmentByTag("NowPlayingDialog")
                assertNotNull(dialog)
                assertTrue(dialog is NowPlayingBottomSheetDialogFragment)
            }
        }
    }
}
