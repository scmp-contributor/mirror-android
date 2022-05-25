package com.scmp.mirror

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.scmp.mirror.model.TrackData
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** init logger */
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        setContentView(R.layout.activity_main)
        /** init the mirror api with below one line code */
        MirrorAPI(application = this.application, domain = "www.scmp.com", isDebug = true)

        val pingButton = findViewById<Button>(R.id.main_activity_ping_button)
        pingButton.setOnClickListener {
            MirrorAPI.instance.ping(TrackData(path = "testing.com"))
        }

        val clickButton = findViewById<Button>(R.id.main_activity_click_button)
        clickButton.setOnClickListener {
            MirrorAPI.instance.click(TrackData(path = "testing.com"))
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        MirrorAPI.instance.dispatchTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}