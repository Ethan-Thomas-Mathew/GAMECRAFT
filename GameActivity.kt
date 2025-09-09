package com.example.flappyball

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var pauseButton: ImageButton
    private lateinit var pauseMenu: LinearLayout
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        gameView = findViewById(R.id.gameView)
        pauseButton = findViewById(R.id.pauseButton)
        pauseMenu = findViewById(R.id.pauseMenu)
        prefs = getSharedPreferences("FlappyBallPrefs", Context.MODE_PRIVATE)


        val difficultyStr = intent.getStringExtra("DIFFICULTY") ?: "MEDIUM"
        gameView.setDifficulty(Difficulty.valueOf(difficultyStr))

        setupPauseControls()
    }

    private fun setupPauseControls() {
        pauseButton.setOnClickListener {
            gameView.pause()
            pauseButton.visibility = View.GONE
            pauseMenu.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.resumeButton).setOnClickListener {
            gameView.resume()
            pauseButton.visibility = View.VISIBLE
            pauseMenu.visibility = View.GONE
        }

        findViewById<Button>(R.id.themesButton).setOnClickListener {
            showThemeDialog()
        }

        findViewById<Button>(R.id.modeButton).setOnClickListener {
            showModeDialog()
        }
    }

    private fun saveThemeColors(editor: SharedPreferences.Editor, bgColor: Int, ballColor: Int, pipeColor: Int, pipeBorder: Int, textColor: Int, gameOver: Int) {
        editor.putInt("bgColor", bgColor)
        editor.putInt("ballColor", ballColor)
        editor.putInt("pipeColor", pipeColor)
        editor.putInt("pipeBorderColor", pipeBorder)
        editor.putInt("textColor", textColor)
        editor.putInt("gameOverColor", gameOver)
    }


    private fun showThemeDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_theme_selector, null)
        builder.setView(view)

        val themeGroup = view.findViewById<RadioGroup>(R.id.themeRadioGroup)
        val customLayout = view.findViewById<LinearLayout>(R.id.customThemeLayout)
        val bgColorGroup = view.findViewById<RadioGroup>(R.id.bgColorGroup)
        val ballColorGroup = view.findViewById<RadioGroup>(R.id.ballColorGroup)
        val pipeColorGroup = view.findViewById<RadioGroup>(R.id.pipeColorGroup)

        val colorOptions = listOf(
            R.color.pastel_sky, R.color.pastel_yellow, R.color.pastel_green,
            R.color.pastel_sunset_bg, R.color.pastel_sunset_obj, R.color.pastel_mint_bg,
            R.color.pastel_mint_obj, R.color.pastel_lavender_bg, R.color.pastel_peach_obj, R.color.pastel_shadow
        )

        populateColorGroup(bgColorGroup, colorOptions)
        populateColorGroup(ballColorGroup, colorOptions)
        populateColorGroup(pipeColorGroup, colorOptions)

        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            customLayout.visibility = if (checkedId == R.id.radioCustom) View.VISIBLE else View.GONE
        }

        val currentBg = prefs.getInt("bgColor", R.color.pastel_sky)
        when(currentBg) {
            R.color.pastel_sky -> themeGroup.check(R.id.radioPastelSky)
            R.color.pastel_sunset_bg -> themeGroup.check(R.id.radioSunset)
            R.color.pastel_mint_bg -> themeGroup.check(R.id.radioMint)
            else -> {
                themeGroup.check(R.id.radioCustom)
                selectColorInGroup(bgColorGroup, currentBg)
                selectColorInGroup(ballColorGroup, prefs.getInt("ballColor", R.color.pastel_yellow))
                selectColorInGroup(pipeColorGroup, prefs.getInt("pipeColor", R.color.pastel_green))
            }
        }


        builder.setPositiveButton("Apply") { dialog, _ ->
            val editor = prefs.edit()
            when (themeGroup.checkedRadioButtonId) {
                R.id.radioPastelSky -> saveThemeColors(editor, R.color.pastel_sky, R.color.pastel_yellow, R.color.pastel_green, R.color.pastel_shadow, R.color.pastel_text_dark, R.color.pastel_red)
                R.id.radioSunset -> saveThemeColors(editor, R.color.pastel_sunset_bg, R.color.pastel_yellow, R.color.pastel_sunset_obj, R.color.pastel_shadow, R.color.pastel_text_dark, R.color.pastel_red)
                R.id.radioMint -> saveThemeColors(editor, R.color.pastel_mint_bg, R.color.pastel_yellow, R.color.pastel_mint_obj, R.color.pastel_shadow, R.color.pastel_text_dark, R.color.pastel_red)
                R.id.radioCustom -> {
                    saveThemeColors(
                        editor,
                        getSelectedColor(bgColorGroup, R.color.pastel_sky),
                        getSelectedColor(ballColorGroup, R.color.pastel_yellow),
                        getSelectedColor(pipeColorGroup, R.color.pastel_green),
                        R.color.pastel_shadow,
                        R.color.pastel_text_dark,
                        R.color.pastel_red
                    )
                }
            }

            editor.apply()
            gameView.resetGame()
            pauseButton.visibility = View.VISIBLE
            pauseMenu.visibility = View.GONE
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    private fun showModeDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_mode_selector, null)
        builder.setView(view)

        val modeGroup = view.findViewById<RadioGroup>(R.id.modeRadioGroup)
        val customLayout = view.findViewById<LinearLayout>(R.id.customModeLayout)
        val speedSlider = view.findViewById<Slider>(R.id.speedSlider)
        val gapSlider = view.findViewById<Slider>(R.id.gapSlider)
        val ballSizeSlider = view.findViewById<Slider>(R.id.ballSizeSlider)

        modeGroup.setOnCheckedChangeListener { _, checkedId ->
            customLayout.visibility = if (checkedId == R.id.radioCustom) View.VISIBLE else View.GONE
        }

        val currentMode = prefs.getString("gameMode", "MEDIUM") ?: "MEDIUM"
        when(currentMode) {
            "EASY" -> modeGroup.check(R.id.radioEasy)
            "MEDIUM" -> modeGroup.check(R.id.radioMedium)
            "HARD" -> modeGroup.check(R.id.radioHard)
            "CUSTOM" -> {
                modeGroup.check(R.id.radioCustom)
                speedSlider.value = prefs.getFloat("customSpeed", 8f)
                gapSlider.value = prefs.getFloat("customGap", 600f)
                ballSizeSlider.value = prefs.getFloat("customBallSize", 50f)
            }
        }

        builder.setPositiveButton("Apply") { dialog, _ ->
            val editor = prefs.edit()
            when (modeGroup.checkedRadioButtonId) {
                R.id.radioEasy -> editor.putString("gameMode", "EASY")
                R.id.radioMedium -> editor.putString("gameMode", "MEDIUM")
                R.id.radioHard -> editor.putString("gameMode", "HARD")
                R.id.radioCustom -> {
                    editor.putString("gameMode", "CUSTOM")
                    editor.putFloat("customSpeed", speedSlider.value)
                    editor.putFloat("customGap", gapSlider.value)
                    editor.putFloat("customBallSize", ballSizeSlider.value)
                }
            }
            editor.apply()
            gameView.resetGame()
            pauseButton.visibility = View.VISIBLE
            pauseMenu.visibility = View.GONE
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    private fun populateColorGroup(group: RadioGroup, colors: List<Int>) {
        colors.forEach { colorResId ->
            val radioButton = RadioButton(this)
            radioButton.id = colorResId
            val buttonSize = (48 * resources.displayMetrics.density).toInt()
            val params = RadioGroup.LayoutParams(buttonSize, buttonSize)
            params.setMargins(8, 8, 8, 8)
            radioButton.layoutParams = params
            radioButton.setBackgroundColor(ContextCompat.getColor(this, colorResId))
            radioButton.buttonDrawable = null
            group.addView(radioButton)
        }
    }

    private fun getSelectedColor(group: RadioGroup, default: Int): Int {
        return if (group.checkedRadioButtonId != -1) group.checkedRadioButtonId else default
    }

    private fun selectColorInGroup(group: RadioGroup, colorResId: Int) {
        val button = group.findViewById<RadioButton>(colorResId)
        button?.isChecked = true
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}