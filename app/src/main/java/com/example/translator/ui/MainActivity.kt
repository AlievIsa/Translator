package com.example.translator.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.example.translator.R
import com.example.translator.data.remote.authentication.models.User
import com.example.translator.domain.Repository.Companion.SHARED_PREFS_KEY
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var themeSwitch: Switch
    private lateinit var userInfo: ConstraintLayout
    private lateinit var authButton: MaterialButton
    private lateinit var exitButton: ImageButton

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (loadThemePreference()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        initAll()
        setListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { user ->
                    prepareDrawerHeader(user)
                }
            }
        }
    }

    private fun initAll() {
        toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        drawerLayout = findViewById(R.id.drawer_layout)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView = findViewById(R.id.nav_view)

        userInfo = navigationView.getHeaderView(0).findViewById(R.id.authorized_header)
        authButton = navigationView.getHeaderView(0).findViewById(R.id.action_authorize)
        exitButton = navigationView.getHeaderView(0).findViewById(R.id.action_exit)

        val menuItem = navigationView.menu.findItem(R.id.change_theme)
        themeSwitch = menuItem.actionView as Switch
        themeSwitch.isChecked = loadThemePreference()
    }

    private fun setListeners() {
        authButton.setOnClickListener{
            navController.navigate(R.id.authFragment)
        }

        exitButton.setOnClickListener {
            signOut()
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_selected -> {
                    navController.navigate(R.id.selectedFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) {
                drawerToggle.syncState()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            saveThemePreference(isChecked)
        }
    }

    private fun prepareDrawerHeader(currentUser : User?) {
        if (currentUser == null) {
            userInfo.visibility = View.GONE
            authButton.visibility = View.VISIBLE
        } else {
            userInfo.visibility = View.VISIBLE
            authButton.visibility = View.GONE

            val userImage = userInfo.findViewById<ImageView>(R.id.user_avatar)
            val userName = userInfo.findViewById<TextView>(R.id.user_name)
            Log.d("Glide", "uri ${currentUser.photoUrl} ")
            Glide.with(this)
                .load(currentUser.photoUrl.toString())
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(userImage)
            userName.text = currentUser.name
        }
    }

    private fun signOut() {
        viewModel.signOut()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.syncState()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item) && navController.currentDestination?.id == R.id.homeFragment) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        val sharedPrefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean("dark_mode", isDarkMode)
            apply()
        }
    }

    private fun loadThemePreference(): Boolean {
        val sharedPrefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("dark_mode", false)
    }
}