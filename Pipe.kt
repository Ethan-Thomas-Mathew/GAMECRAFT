package com.example.flappyball

import android.graphics.RectF

class Pipe(
    var x: Float,
    private val pipeWidth: Float,
    gapTop: Float,
    gapHeight: Float,
    screenHeight: Float,
    val speed: Float
) {
    var isScored = false

    val topPipe = RectF(x, 0f, x + pipeWidth, gapTop)
    val bottomPipe = RectF(x, gapTop + gapHeight, x + pipeWidth, screenHeight)

    // This new update function ensures the pipe's position and its hitboxes move together.
    fun update() {
        x -= speed
        topPipe.left = x
        topPipe.right = x + pipeWidth
        bottomPipe.left = x
        bottomPipe.right = x + pipeWidth
    }

    fun isColliding(ball: Ball): Boolean {
        val ballHitbox = RectF(
            ball.x - ball.radius,
            ball.y - ball.radius,
            ball.x + ball.radius,
            ball.y + ball.radius
        )
        // This non-destructive check is correct and no longer causes visual bugs.
        return RectF.intersects(topPipe, ballHitbox) || RectF.intersects(bottomPipe, ballHitbox)
    }
}