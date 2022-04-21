package com.satdev.rockpaperscisors.util

sealed class PlayStatus<T>(
    val results: T? = null) {
    class Player1Playing<T>(results: T? = null) : PlayStatus<T>(results)
    class Player2Playing<T>(results: T? = null) : PlayStatus<T>(results)
    class GameTie<T>(results: T? = null) : PlayStatus<T>(results)
    class NoInput<T>(results: T? = null) : PlayStatus<T>(results)
    class GameWon<T>(results: T) : PlayStatus<T>(results)
    class GameFinished<T>(results: T) : PlayStatus<T>(results)

}
