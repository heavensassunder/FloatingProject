package com.example.myempty.floatingproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myempty.floatingproject.store.SlimeDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var apiInput: EditText
    private lateinit var saveBtn: Button
    private lateinit var testBtn: Button
    private val scope = CoroutineScope(Dispatchers.Main)

    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // no direct result; the user must accept in Settings
        Toast.makeText(this, "Returned from overlay permission settings", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        apiInput = findViewById(R.id.api_key_input)
        saveBtn = findViewById(R.id.save_button)
        testBtn = findViewById(R.id.test_button)

        saveBtn.setOnClickListener {
            val key = apiInput.text.toString().trim()
            if (key.isEmpty()) {
                Toast.makeText(this, "Enter API key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            scope.launch {
                SlimeDataStore.saveApiKey(applicationContext, key)
                Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
            }
        }

        testBtn.setOnClickListener {
            scope.launch {
                val key = SlimeDataStore.apiKeyFlow(applicationContext).collect { } // placeholder
            }
            // simple check
            Toast.makeText(this, "Test presses - AI test will run from background service", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.open_overlay_settings)?.setOnClickListener {
            requestOverlayPermission()
        }

        // Start FloatingService if overlay permission granted
        if (Settings.canDrawOverlays(this)) {
            startService(Intent(this, com.example.myempty.floatingproject.service.FloatingService::class.java))
        } else {
            requestOverlayPermission()
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            overlayPermissionLauncher.launch(intent)
        }
    }

    companion object {
        // MediaProjection result flow: you will need to start this intent to get screen capture permissions.
        // val REQUEST_MEDIA_PROJECTION = 999
    }
}
