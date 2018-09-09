package com.hackathon.fellas.homedepotapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 123
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
    }

    fun goToHelp() {
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
    }

    fun goToMyList() {
        val intent = Intent(this, MyListActivity::class.java)
        startActivity(intent)
    }

    fun goToSearch() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }
}
