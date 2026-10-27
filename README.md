# Flappy Ball (Gamecraft)

A Flappy Bird-style Android game written in Kotlin. Tap to flap a ball through gaps between pipes and beat your high score.

**Live demo:** https://ethan-thomas-mathew.github.io/GAMECRAFT/

The demo is a web version of the game. It uses the same physics, difficulty values and colour themes as the Kotlin source. The Android app itself is not run in the browser.

## Features

- Easy, Medium, Hard and Custom modes (custom pipe speed, pipe gap and ball size)
- Pastel Sky, Sunset and Mint themes, plus a custom colour theme
- Pause menu to change the theme and mode during a game
- High score and settings saved on the device with `SharedPreferences`
- Collision detection and a game loop drawn directly on an Android `Canvas`

## Tech

- Kotlin, Android SDK
- Custom `View` with `Canvas` rendering
- Material Components (sliders, dialogs)
- Demo: plain HTML, CSS and JavaScript on a `<canvas>`, hosted on GitHub Pages

## Source files

| File | What it does |
| --- | --- |
| `GameView.kt` | Game loop, drawing, scoring, game states and saved settings |
| `GameActivity.kt` | Pause menu, theme dialog and mode dialog |
| `MainMenuActivity.kt` | Start screen with the difficulty slider |
| `Ball.kt` | Ball gravity and flap |
| `Pipe.kt` | Pipe movement and collision boxes |
| `Difficulty.kt` | Easy, Medium and Hard levels |
| `index.html` | The web demo |

## How to play

Tap the screen (or click, or press Space) to flap. Pass through the gaps to score. Press P to pause in the demo.
