package com.example.flappyball

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

class MainMenuActivity : AppCompatActivity() {

    private var selectedDifficulty = Difficulty.MEDIUM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Set the activity to fullscreen
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val difficultySlider = findViewById<Slider>(R.id.difficultySlider)
        val playButton = findViewById<Button>(R.id.playButton)

        // Listen for changes on the slider to update the difficulty
        difficultySlider.addOnChangeListener { _, value, _ ->
            selectedDifficulty = when (value.toInt()) {
                0 -> Difficulty.EASY
                1 -> Difficulty.MEDIUM
                2 -> Difficulty.HARD
                else -> Difficulty.MEDIUM
            }
        }

        // When the play button is clicked, start the game activity
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java).apply {
                // Pass the selected difficulty to the GameActivity
                putExtra("DIFFICULTY", selectedDifficulty.name)
            }
            startActivity(intent)
        }
    }
}