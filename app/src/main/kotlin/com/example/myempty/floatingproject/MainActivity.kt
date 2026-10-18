package com.example.myempty.floatingproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myempty.floatingproject.service.FloatingService
import com.example.myempty.floatingproject.store.SlimeDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var apiInput: EditText
    private lateinit var saveBtn: Button
    private lateinit var testBtn: Button
    private val scope = CoroutineScope(Dispatchers.Main)

    private val overlayLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Settings.canDrawOverlays(this)) {
            startFloatingServiceIfNeeded()
            Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Overlay permission still not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 13+: request notification permission for foreground service notification
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        apiInput = findViewById(R.id.api_key_input)
        saveBtn = findViewById(R.id.save_button)
        testBtn = findViewById(R.id.test_button)

        saveBtn.setOnClickListener {
            val key = apiInput.text.toString().trim()
            if (key.isEmpty()) {
                Toast.makeText(this, "Enter API key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            scope.launch { SlimeDataStore.saveApiKey(applicationContext, key) }
            Toast.makeText(this, "Saved API key", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.open_overlay_settings)?.setOnClickListener {
            requestOverlayPermission()
        }

        // If permission already granted, start service immediately
        if (Settings.canDrawOverlays(this)) {
            startFloatingServiceIfNeeded()
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            overlayLauncher.launch(intent)
        } else {
            startFloatingServiceIfNeeded()
        }
    }

    private fun startFloatingServiceIfNeeded() {
        val svcIntent = Intent(this, FloatingService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, svcIntent)
        } else {
            startService(svcIntent)
        }
    }
}