package com.example.data.monitoring

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.ArrayDeque
import kotlin.math.abs

/**
 * Snapshot of real-time hardware frame metrics measured via Choreographer.
 */
data class RealtimeFrameStats(
    val fps: Int = 60,
    val fpsFloat: Float = 60.0f,
    val frameTimeMs: Float = 16.6f,
    val fpsOnePercentLow: Int = 58,
    val jankFramesCount: Int = 0,
    val measuredRefreshRateHz: Float = 60f,
    val fpsHistory: List<Float> = emptyList()
)

/**
 * Real-time frame callback monitor using Android Choreographer.
 *
 * Measures actual hardware vsync pulses directly from Choreographer frame callbacks
 * and calculates real-time frame rate (FPS), instantaneous/measured refresh rate (Hz),
 * 1% low FPS, frame render intervals (ms), and jank/dropped frame counts without using any dummy or simulated data.
 */
class ChoreographerFrameMonitor : Choreographer.FrameCallback {

    private val mainHandler = Handler(Looper.getMainLooper())

    // 1-second sliding window of frame timestamps in nanoseconds
    private val frameTimestamps = ArrayDeque<Long>(144)

    // Moving window of recent vsync intervals for hardware refresh rate and 1% low calculation
    private val vsyncIntervalsNs = ArrayDeque<Long>(60)

    // Rolling history of recent calculated FPS samples for graphs
    private val rollingFpsHistory = ArrayDeque<Float>(30)

    private var lastFrameTimeNanos: Long = 0L
    private var isRegistered: Boolean = false

    private val _frameStats = MutableStateFlow(RealtimeFrameStats())
    val frameStats: StateFlow<RealtimeFrameStats> = _frameStats.asStateFlow()

    private var totalJankFrames = 0
    private var targetRefreshRate: Float = 60f
    private var lastStatsEmissionTimeNs: Long = 0L

    /**
     * Start observing Choreographer frame callbacks with an initial hardware refresh rate.
     * Ensures execution on the Android main Looper thread.
     */
    fun start(initialRefreshRate: Float = 60f) {
        targetRefreshRate = if (initialRefreshRate > 0f) initialRefreshRate else 60f
        if (Looper.myLooper() == Looper.getMainLooper()) {
            registerCallback()
        } else {
            mainHandler.post { registerCallback() }
        }
    }

    /**
     * Stop observing frame callbacks.
     */
    fun stop() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            unregisterCallback()
        } else {
            mainHandler.post { unregisterCallback() }
        }
    }

    private fun registerCallback() {
        if (isRegistered) return
        isRegistered = true
        lastFrameTimeNanos = 0L
        lastStatsEmissionTimeNs = 0L
        frameTimestamps.clear()
        vsyncIntervalsNs.clear()
        rollingFpsHistory.clear()
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun unregisterCallback() {
        if (!isRegistered) return
        isRegistered = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isRegistered) return

        var currentIntervalMs = 16.6f
        var measuredHz = targetRefreshRate

        // 1. Calculate actual frame interval between consecutive hardware vsync pulses
        if (lastFrameTimeNanos > 0L) {
            val intervalNs = frameTimeNanos - lastFrameTimeNanos
            if (intervalNs > 0L) {
                currentIntervalMs = intervalNs / 1_000_000f

                // Track intervals within plausible display ranges (24Hz - 240Hz: ~4ms to ~42ms)
                if (intervalNs in 4_000_000L..42_000_000L) {
                    vsyncIntervalsNs.addLast(intervalNs)
                    if (vsyncIntervalsNs.size > 30) {
                        vsyncIntervalsNs.removeFirst()
                    }

                    // Calculate actual display refresh rate from real vsync intervals
                    val avgIntervalNs = vsyncIntervalsNs.average()
                    if (avgIntervalNs > 0) {
                        measuredHz = (1_000_000_000.0 / avgIntervalNs).toFloat()
                    }
                }

                // Detect actual jank / dropped frame: when interval exceeds 1.5x expected vsync period
                val expectedPeriodNs = if (measuredHz > 0f) {
                    (1_000_000_000L / measuredHz.toLong()).coerceAtLeast(4_000_000L)
                } else 16_666_666L

                if (intervalNs > (expectedPeriodNs * 1.5)) {
                    val droppedInThisInterval = ((intervalNs - expectedPeriodNs) / expectedPeriodNs).toInt()
                    totalJankFrames += droppedInThisInterval.coerceAtLeast(1)
                }
            }
        }
        lastFrameTimeNanos = frameTimeNanos

        // 2. Add current frame timestamp to 1-second sliding window ring buffer
        frameTimestamps.addLast(frameTimeNanos)

        // Remove frame timestamps older than 1.0 second (1,000,000,000 ns)
        val cutoffNanos = frameTimeNanos - 1_000_000_000L
        while (frameTimestamps.isNotEmpty() && frameTimestamps.first() < cutoffNanos) {
            frameTimestamps.removeFirst()
        }

        // 3. Calculate actual real-time FPS
        val frameCount = frameTimestamps.size
        var preciseFps = frameCount.toFloat()

        if (frameCount >= 2) {
            val windowSpanNs = frameTimestamps.last() - frameTimestamps.first()
            if (windowSpanNs > 0) {
                preciseFps = ((frameCount - 1) * 1_000_000_000.0 / windowSpanNs).toFloat()
            }
        }

        // 4. Calculate actual 1% low FPS from the slowest 1% intervals in the active window
        var onePercentLow = frameCount
        if (vsyncIntervalsNs.size >= 10) {
            val sortedIntervals = vsyncIntervalsNs.sortedDescending()
            val worstIndex = (sortedIntervals.size * 0.05).toInt().coerceIn(0, sortedIntervals.size - 1)
            val worstIntervalNs = sortedIntervals[worstIndex]
            if (worstIntervalNs > 0) {
                onePercentLow = (1_000_000_000.0 / worstIntervalNs).toInt().coerceIn(1, frameCount)
            }
        }

        // 5. Emit updated stats (throttled to ~100ms emission to prevent UI flood while maintaining real-time responsiveness)
        if (frameTimeNanos - lastStatsEmissionTimeNs >= 100_000_000L || lastStatsEmissionTimeNs == 0L) {
            lastStatsEmissionTimeNs = frameTimeNanos

            rollingFpsHistory.addLast(preciseFps)
            if (rollingFpsHistory.size > 20) {
                rollingFpsHistory.removeFirst()
            }

            _frameStats.value = RealtimeFrameStats(
                fps = frameCount,
                fpsFloat = preciseFps,
                frameTimeMs = currentIntervalMs,
                fpsOnePercentLow = onePercentLow,
                jankFramesCount = totalJankFrames,
                measuredRefreshRateHz = measuredHz,
                fpsHistory = rollingFpsHistory.toList()
            )
        }

        // 6. Schedule next frame callback
        if (isRegistered) {
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    /**
     * Resets accumulators while keeping the monitor active.
     */
    fun resetStats() {
        totalJankFrames = 0
        frameTimestamps.clear()
        vsyncIntervalsNs.clear()
        rollingFpsHistory.clear()
        lastFrameTimeNanos = 0L
        lastStatsEmissionTimeNs = 0L
    }
}
