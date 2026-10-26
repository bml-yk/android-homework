package cn.edu.sicnucs.bml.homework4_mul_activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Activity4 : AppCompatActivity() {

    private lateinit var tvTime: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var accumulatedMillis = 0L
    private var runningSinceRealtime = 0L
    private var isRunning = false

    private val updateTimerThread = object : Runnable {
        override fun run() {
            renderElapsedTime()
            if (isRunning) {
                handler.postDelayed(this, TIMER_REFRESH_INTERVAL_MS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_4)
        title = getString(R.string.act4_title)

        tvTime = findViewById(R.id.tv_time)
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnStop = findViewById<Button>(R.id.btn_stop)
        val btnReset = findViewById<Button>(R.id.btn_reset)

        accumulatedMillis = savedInstanceState?.getLong(getString(R.string.state_accumulated_key)) ?: 0L
        runningSinceRealtime = savedInstanceState?.getLong(getString(R.string.state_running_since_key)) ?: 0L
        isRunning = savedInstanceState?.getBoolean(getString(R.string.state_is_running_key)) ?: false
        renderElapsedTime()

        btnStart.setOnClickListener {
            if (!isRunning) {
                runningSinceRealtime = SystemClock.elapsedRealtime()
                isRunning = true
                handler.removeCallbacks(updateTimerThread)
                handler.post(updateTimerThread)
            }
        }

        btnStop.setOnClickListener {
            accumulatedMillis = currentElapsedMillis()
            isRunning = false
            handler.removeCallbacks(updateTimerThread)
            renderElapsedTime()
        }

        btnReset.setOnClickListener {
            isRunning = false
            handler.removeCallbacks(updateTimerThread)
            accumulatedMillis = 0L
            runningSinceRealtime = 0L
            renderElapsedTime()
        }
    }

    override fun onStart() {
        super.onStart()
        renderElapsedTime()
        if (isRunning) {
            handler.removeCallbacks(updateTimerThread)
            handler.post(updateTimerThread)
        }
    }

    override fun onStop() {
        handler.removeCallbacks(updateTimerThread)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(getString(R.string.state_accumulated_key), accumulatedMillis)
        outState.putLong(getString(R.string.state_running_since_key), runningSinceRealtime)
        outState.putBoolean(getString(R.string.state_is_running_key), isRunning)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateTimerThread)
        super.onDestroy()
    }

    private fun currentElapsedMillis(): Long {
        if (!isRunning) return accumulatedMillis
        return accumulatedMillis + (SystemClock.elapsedRealtime() - runningSinceRealtime)
    }

    private fun renderElapsedTime() {
        val totalMillis = currentElapsedMillis()
        val totalSeconds = totalMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val milliseconds = totalMillis % 1000
        tvTime.text = getString(R.string.stopwatch_time_format, minutes, seconds, milliseconds)
    }

    private companion object {
        const val TIMER_REFRESH_INTERVAL_MS = 10L
    }
}
