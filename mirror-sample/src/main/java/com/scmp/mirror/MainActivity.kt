package com.scmp.mirror

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.scmp.mirror.model.TrackData

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mirrorAPI = MirrorAPI(context = this, domain = "www.scmp.com", isDebug = true)
        val pingButton = findViewById<Button>(R.id.main_activity_ping_button)
        pingButton.setOnClickListener {
            mirrorAPI.ping(TrackData(path = "testing.com"))
        }

        val clickButton = findViewById<Button>(R.id.main_activity_click_button)
        clickButton.setOnClickListener {
            mirrorAPI.click(TrackData(path = "testing.com"))
        }
    }
}