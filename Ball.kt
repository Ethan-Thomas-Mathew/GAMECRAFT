package com.example.flappyball

class Ball(var x: Float, var y: Float, var radius: Float = 50f) { // Radius is now configurable
    private var velocityY = 0f
    private val gravity = 1.5f
    private val lift = -25f

    // Update the ball's position based on gravity
    fun update() {
        velocityY += gravity
        y += velocityY
    }

    // Apply an upward force to the ball
    fun flap() {
        velocityY = lift
    }
}