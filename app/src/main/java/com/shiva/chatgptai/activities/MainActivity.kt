package com.shiva.chatgptai.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.shiva.chatgptai.fragments.AboutFragment
import com.shiva.chatgptai.fragments.HomeFragment
import com.shiva.chatgptai.fragments.SettingFragment
import com.shiva.chatgptai.fragments.ShareFragment
import com.shiva.loginandsignup.R
import com.shiva.loginandsignup.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding : ActivityMainBinding

    companion object {
        private const val PREFS_KEY = "my_prefs_key" // Replace with your preferred key
        private const val IS_LOGGED_IN_KEY = "is_logged_in" // Replace with your preferred key
        private const val LOGIN_REQUEST_CODE = 1001 // Replace with your preferred request code
    }

//    private lateinit var userName: TextView
//    private lateinit var logout: Button
    private lateinit var gClient: GoogleSignInClient
    private lateinit var gOptions: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        gOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        gClient = GoogleSignIn.getClient(this, gOptions)

//        val gAccount = GoogleSignIn.getLastSignedInAccount(this)
//        if (gAccount != null) {
//            val gName = gAccount.displayName
//            userName.text = gName
//        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_settings -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment()).commit()
            R.id.nav_share -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ShareFragment()).commit()
            R.id.nav_about -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutFragment()).commit()
            R.id.nav_logout -> {
                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()

                // Sign out the user from Google Sign-In
                gClient.signOut().addOnCompleteListener(this, OnCompleteListener {
                    // Clear the logged-in status in shared preferences
                    val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putBoolean(IS_LOGGED_IN_KEY, false)
                    editor.apply()

                    // Finish the current activity and start LoginActivity
                    finish()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                })
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}