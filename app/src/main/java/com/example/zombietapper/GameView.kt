package com.example.zombietapper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val zombies = mutableListOf<Zombie>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var score = 0
    private var lives = 3
    private var gameOver = false
    private var lastSpawn = 0L
    private var spawnInterval = 1500L
    private var baseSpeed = 3f

    private val zombieEmoji = "🧟"
    private val heartEmoji = "❤️"

    init {
        paint.textSize = 60f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!gameOver) {
            update()
        }

        paint.textSize = 100f
        for (z in zombies) {
            canvas.drawText(zombieEmoji, z.x, z.y, paint)
        }

        paint.color = Color.WHITE
        paint.textSize = 60f
        canvas.drawText("Score: $score", 40f, 80f, paint)

        paint.textSize = 50f
        val hearts = heartEmoji.repeat(lives)
        canvas.drawText(hearts, width - 300f, 80f, paint)

        if (gameOver) {
            paint.textSize = 120f
            paint.color = Color.RED
            canvas.drawText("GAME OVER", width / 2f - 350f, height / 2f, paint)

            paint.textSize = 60f
            paint.color = Color.WHITE
            canvas.drawText("Tap to restart", width / 2f - 200f, height / 2f + 100f, paint)
        }

        invalidate()
    }

    private fun update() {
        val now = System.currentTimeMillis()

        if (now - lastSpawn > spawnInterval) {
            spawnZombie()
            lastSpawn = now
        }

        val iterator = zombies.iterator()
        while (iterator.hasNext()) {
            val z = iterator.next()
            z.x += z.speed

            if (z.x > width) {
                iterator.remove()
                lives--
                if (lives <= 0) {
                    gameOver = true
                }
            }
        }
    }

    private fun spawnZombie() {
        val size = 100f
        val y = Random.nextFloat() * (height - 300f) + 250f

        zombies.add(
            Zombie(
                x = -size,
                y = y,
                speed = baseSpeed + Random.nextFloat() * 2f,
                size = size
            )
        )

        if (score > 0 && score % 5 == 0) {
            baseSpeed += 0.3f
            spawnInterval = maxOf(400L, spawnInterval - 50L)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                resetGame()
                return true
            }

            val tapX = event.x
            val tapY = event.y

            val iterator = zombies.iterator()
            while (iterator.hasNext()) {
                val z = iterator.next()
                if (tapX >= z.x && tapX <= z.x + z.size &&
                    tapY >= z.y - z.size && tapY <= z.y
                ) {
                    iterator.remove()
                    score++
                    vibrate()
                    return true
                }
            }
        }
        return true
    }

    private fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(30)
        }
    }

    private fun resetGame() {
        zombies.clear()
        score = 0
        lives = 3
        gameOver = false
        baseSpeed = 3f
        spawnInterval = 1500L
        lastSpawn = 0L
    }
}
