// BLACK PANTHER — أبوجبل ابن الكوت — @P71GP
package com.blackpanther.app

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var btn: Button
    private lateinit var status: TextView
    private lateinit var timer: TextView
    private lateinit var prefs: android.content.SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            val start = prefs.getLong("start", 0L)
            val diff = System.currentTimeMillis() - start
            val h = TimeUnit.MILLISECONDS.toHours(diff)
            val m = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val s = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            timer.text = String.format("%02d:%02d:%02d", h, m, s)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn = findViewById(R.id.btn)
        status = findViewById(R.id.status)
        timer = findViewById(R.id.timer)
        prefs = getSharedPreferences("bp", Context.MODE_PRIVATE)
        running = prefs.getBoolean("running", false)
        updateUi()
        btn.setOnClickListener { if (running) deactivate() else activate() }
    }

    private fun updateUi() {
        if (running) {
            btn.text = "إيقاف"
            btn.background.setTint(getColor(R.color.bp_red))
            status.text = "مفعل ✅"
            handler.post(tick)
        } else {
            btn.text = "تفعيل"
            btn.background.setTint(getColor(R.color.bp_green))
            status.text = "غير مفعل"
            handler.removeCallbacks(tick)
            timer.text = "00:00:00"
        }
    }

    private fun activate() {
        prefs.edit().putBoolean("running", true)
            .putLong("start", System.currentTimeMillis()).apply()
        running = true
        updateUi()
        clearCache()
        killBackground()
        openPrivateDns()
        Toast.makeText(this, "تم التفعيل — اكتب one.one.one.one", Toast.LENGTH_LONG).show()
    }

    private fun deactivate() {
        prefs.edit().putBoolean("running", false).apply()
        running = false
        updateUi()
    }

    private fun clearCache() {
        try {
            cacheDir.listFiles()?.forEach { it.deleteRecursively() }
            externalCacheDir?.listFiles()?.forEach { it.deleteRecursively() }
        } catch (_: Exception) {}
    }

    private fun killBackground() {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.killBackgroundProcesses(packageName)
        } catch (_: Exception) {}
    }

    private fun openPrivateDns() {
        try { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tick)
    }
}
