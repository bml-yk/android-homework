package cn.edu.sicnucs.bml.homework6_fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : Screen(), AppStateHost {
    private lateinit var historyStore: HistoryStore
    override lateinit var appState: AppState
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLayout(R.layout.activity_main)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))

        historyStore = HistoryStore(this)
        appState = historyStore.load()

        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        navigation.setOnItemSelectedListener { item ->
            val destination = when (item.itemId) {
                R.id.nav_profile -> FragmentDestination(
                    ProfileFragment.TAG,
                    ProfileFragment::class.java
                )
                R.id.nav_two_cards -> FragmentDestination(
                    TwoCardGameFragment.TAG,
                    TwoCardGameFragment::class.java
                )
                R.id.nav_three_cards -> FragmentDestination(
                    ThreeCardGameFragment.TAG,
                    ThreeCardGameFragment::class.java
                )
                else -> return@setOnItemSelectedListener false
            }
            showDestination(destination)
            true
        }

        if (savedInstanceState == null) {
            navigation.selectedItemId = R.id.nav_profile
        } else if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            navigation.selectedItemId = navigation.selectedItemId.takeIf { it != 0 }
                ?: R.id.nav_profile
        }
    }

    private fun showDestination(destination: FragmentDestination) {
        if (supportFragmentManager.findFragmentByTag(destination.tag)?.isVisible == true) {
            return
        }
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            destination.fragmentClass.name
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, destination.tag)
            .commit()
    }

    override fun persistState() {
        historyStore.save(appState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.ok, null)
                .show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        persistState()
        super.onStop()
    }

    private data class FragmentDestination(
        val tag: String,
        val fragmentClass: Class<out Fragment>
    )
}
