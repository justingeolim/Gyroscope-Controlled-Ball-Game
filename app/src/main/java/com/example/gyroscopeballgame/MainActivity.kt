package com.example.gyroscopeballgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gyroscopeballgame.ui.theme.GyroscopeBallGameTheme

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null

    // ball position
    private var ballX by mutableStateOf(200f)
    private var ballY by mutableStateOf(400f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        setContent {
            GyroscopeBallGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BallGame(
                        ballX = ballX,
                        ballY = ballY,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val speed = 15f

        val newX = ballX + event.values[1] * speed
        val newY = ballY - event.values[0] * speed

        // move each axis separately so ball slides along walls
        if (!hitsWall(newX, ballY)) ballX = newX
        if (!hitsWall(ballX, newY)) ballY = newY
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // collision
    private fun hitsWall(x: Float, y: Float): Boolean {
        val r = 30f
        return getWalls().any { wall ->
            x + r > wall.left && x - r < wall.right &&
                    y + r > wall.top && y - r < wall.bottom
        }
    }

    // build walls depending on the phone size
    private fun getWalls(): List<Rect> {
        val w = 1080f
        val h = 2200f
        val t = 40f
        return listOf(
            Rect(0f, 0f, w, t),
            Rect(0f, 0f, t, h),
            Rect(w - t, 0f, w, h),
            Rect(0f, h - t, w, h),
            Rect(w * 0.3f, h * 0.15f, w * 0.3f + t, h * 0.45f),
            Rect(w * 0.6f, h * 0.1f, w * 0.6f + t, h * 0.35f),
            Rect(w * 0.2f, h * 0.5f, w * 0.6f, h * 0.5f + t),
            Rect(w * 0.1f, h * 0.7f, w * 0.4f, h * 0.7f + t),
            Rect(w * 0.6f, h * 0.65f, w * 0.6f + t, h * 0.85f)
        )
    }
}

@Composable
fun BallGame(ballX: Float, ballY: Float, modifier: Modifier = Modifier) {
    val ballRadius = 30f
    val walls = listOf(
        Rect(0f, 0f, 1080f, 40f),
        Rect(0f, 0f, 40f, 2200f),
        Rect(1040f, 0f, 1080f, 2200f),
        Rect(0f, 2160f, 1080f, 2200f),
        Rect(1080f * 0.3f, 2200f * 0.15f, 1080f * 0.3f + 40f, 2200f * 0.45f),
        Rect(1080f * 0.6f, 2200f * 0.1f, 1080f * 0.6f + 40f, 2200f * 0.35f),
        Rect(1080f * 0.2f, 2200f * 0.5f, 1080f * 0.6f, 2200f * 0.5f + 40f),
        Rect(1080f * 0.1f, 2200f * 0.7f, 1080f * 0.4f, 2200f * 0.7f + 40f),
        Rect(1080f * 0.6f, 2200f * 0.65f, 1080f * 0.6f + 40f, 2200f * 0.85f)
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // draw walls
        walls.forEach { wall ->
            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(wall.left, wall.top),
                size = Size(wall.width, wall.height)
            )
        }

        // draw ball
        drawCircle(
            color = Color.Red,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )

        // draw goal
        drawCircle(
            color = Color.Green,
            radius = ballRadius,
            center = Offset(1080f * 0.85f, 2200f * 0.9f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BallGamePreview() {
    GyroscopeBallGameTheme {
        BallGame(ballX = 200f, ballY = 400f)
    }
}