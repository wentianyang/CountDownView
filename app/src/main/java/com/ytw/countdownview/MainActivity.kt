package com.ytw.countdownview

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ytw.countdownview.widget.CountDownView

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    startCount()
    println("是否被堵塞...")
  }

  private fun startCount() {
    Log.d("MainActivity", "real time is ${SystemClock.uptimeMillis()}")
    countdown(1569957140000 - System.currentTimeMillis())
  }

  fun countdown(currTime: Long) = run { findViewById<CountDownView>(R.id.count_down).start(currTime) }
}
