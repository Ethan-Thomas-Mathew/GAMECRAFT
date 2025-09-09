package com.example.flappyball

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private enum class GameState {
        READY, RUNNING, PAUSED, GAMEOVER
    }

    private var gameState: GameState = GameState.READY
    private var score = 0
    private var highScore = 0
    private var isNewHighScore = false
    private var difficulty: Difficulty = Difficulty.MEDIUM

    private var screenWidth = 0
    private var screenHeight = 0

    private lateinit var ball: Ball
    private val ballPaint = Paint().apply { isAntiAlias = true }
    private val pipes = mutableListOf<Pipe>()
    private val pipePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
    private val pipeBorderPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 10f }

    private var pipeWidth = 200f
    private var pipeGap = 600f
    private var pipeSpeed = 8f
    private var pipeDistance = 0f // New variable for consistent pipe spacing
    private var ballSize = 50f

    private val scorePaint = Paint().apply { textSize = 100f; textAlign = Paint.Align.CENTER; isAntiAlias = true }
    private val gameOverPaint = Paint().apply { textSize = 150f; textAlign = Paint.Align.CENTER; isAntiAlias = true }
    private val infoTextPaint = Paint().apply { textSize = 70f; textAlign = Paint.Align.CENTER; isAntiAlias = true }

    private var backgroundColor: Int = 0
    private val prefs: SharedPreferences = context.getSharedPreferences("FlappyBallPrefs", Context.MODE_PRIVATE)

    init {
        loadSettings()
    }

    fun setDifficulty(selectedDifficulty: Difficulty) {
        difficulty = selectedDifficulty
        prefs.edit().putString("gameMode", difficulty.name).apply()
    }

    private fun loadSettings() {
        highScore = prefs.getInt("highScore", 0)

        backgroundColor = ContextCompat.getColor(context, prefs.getInt("bgColor", R.color.pastel_sky))
        ballPaint.color = ContextCompat.getColor(context, prefs.getInt("ballColor", R.color.pastel_yellow))
        pipePaint.color = ContextCompat.getColor(context, prefs.getInt("pipeColor", R.color.pastel_green))
        pipeBorderPaint.color = ContextCompat.getColor(context, prefs.getInt("pipeBorderColor", R.color.pastel_shadow))
        scorePaint.color = ContextCompat.getColor(context, prefs.getInt("textColor", R.color.pastel_text_dark))
        gameOverPaint.color = ContextCompat.getColor(context, prefs.getInt("gameOverColor", R.color.pastel_red))
        infoTextPaint.color = scorePaint.color

        val mode = prefs.getString("gameMode", difficulty.name)
        when (mode) {
            "EASY" -> { pipeGap = 700f; pipeSpeed = 7f; ballSize = 50f }
            "MEDIUM" -> { pipeGap = 600f; pipeSpeed = 8f; ballSize = 50f }
            "HARD" -> { pipeGap = 500f; pipeSpeed = 10f; ballSize = 50f }
            "CUSTOM" -> {
                pipeSpeed = prefs.getFloat("customSpeed", 8f)
                pipeGap = prefs.getFloat("customGap", 600f)
                ballSize = prefs.getFloat("customBallSize", 50f)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h
        pipeDistance = screenWidth * 0.55f // Set pipe distance based on screen width
        resetGame()
    }

    private fun update() {
        if (gameState != GameState.RUNNING) return
        ball.update()

        if (ball.y - ball.radius < 0 || ball.y + ball.radius > screenHeight) {
            setGameOver()
        }

        val pipesToRemove = mutableListOf<Pipe>()
        for (pipe in pipes) {
            pipe.update() // This now updates the pipe's position AND its hitbox rects
            if (pipe.isColliding(ball)) {
                setGameOver()
            }
            if (pipe.x + pipeWidth < 0) {
                pipesToRemove.add(pipe)
            }
            if (!pipe.isScored && pipe.x + pipeWidth < ball.x) {
                score++
                pipe.isScored = true
            }
        }
        pipes.removeAll(pipesToRemove)

        // New, more reliable pipe spawning logic
        if (pipes.isEmpty() || screenWidth - pipes.last().x >= pipeDistance) {
            spawnPipe()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)
        canvas.drawCircle(ball.x, ball.y, ball.radius, ballPaint)

        for (pipe in pipes) {
            // The pipe's RectF is now always up-to-date, so we just draw it.
            canvas.drawRoundRect(pipe.topPipe, 25f, 25f, pipePaint)
            canvas.drawRoundRect(pipe.bottomPipe, 25f, 25f, pipePaint)
            canvas.drawRoundRect(pipe.topPipe, 25f, 25f, pipeBorderPaint)
            canvas.drawRoundRect(pipe.bottomPipe, 25f, 25f, pipeBorderPaint)
        }

        canvas.drawText(score.toString(), screenWidth / 2f, 150f, scorePaint)

        when (gameState) {
            GameState.READY -> canvas.drawText("Tap to Start", screenWidth / 2f, screenHeight / 2f, infoTextPaint)
            GameState.GAMEOVER -> {
                canvas.drawText("Game Over", screenWidth / 2f, screenHeight / 2f - 150, gameOverPaint)
                if (isNewHighScore) {
                    canvas.drawText("NEW HIGH SCORE: $score", screenWidth / 2f, screenHeight / 2f - 50, scorePaint)
                } else {
                    canvas.drawText("Score: $score", screenWidth / 2f, screenHeight / 2f - 50, scorePaint)
                    canvas.drawText("High Score: $highScore", screenWidth / 2f, screenHeight / 2f + 20, scorePaint)
                }
                canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 120, infoTextPaint)
            }
            else -> {}
        }

        update()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            when (gameState) {
                GameState.READY -> gameState = GameState.RUNNING
                GameState.RUNNING -> ball.flap()
                GameState.GAMEOVER -> resetGame()
                else -> {}
            }
        }
        return true
    }

    private fun spawnPipe() {
        val gapTop = Random.nextFloat() * (screenHeight - pipeGap - 400) + 200
        pipes.add(Pipe(screenWidth.toFloat(), pipeWidth, gapTop, pipeGap, screenHeight.toFloat(), pipeSpeed))
    }

    private fun setGameOver() {
        if (gameState == GameState.RUNNING) { // Prevent multiple calls
            gameState = GameState.GAMEOVER
            if (score > highScore) {
                isNewHighScore = true
                highScore = score
                prefs.edit().putInt("highScore", highScore).apply()
            }
        }
    }

    fun resetGame() {
        loadSettings()
        score = 0
        isNewHighScore = false
        ball = Ball(x = screenWidth / 4f, y = screenHeight / 2f, radius = ballSize)
        pipes.clear()
        gameState = GameState.READY
    }

    fun pause() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED
        }
    }

    fun resume() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING
        }
    }
}