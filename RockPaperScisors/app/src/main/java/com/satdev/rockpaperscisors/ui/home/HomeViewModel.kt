package com.satdev.rockpaperscisors.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.satdev.rockpaperscisors.domain.Player
import com.satdev.rockpaperscisors.domain.RockPapersScissors
import com.satdev.rockpaperscisors.util.PlayStatus

class HomeViewModel : ViewModel() {
    val JUGADOR_1 = "1"
    val JUGADOR_2 = "2"
    val TAG ="SAT_tAG"

    private val p1 = Player("Jugador 1")
    private val p2 = Player("Jugador 2")
    private var game: RockPapersScissors = RockPapersScissors().apply {
        players[0] = p1
        players[1] = p2
    }
    private var turn: String = JUGADOR_1
    private var hasRoundFinsish = false

    val playResult = MutableLiveData<PlayStatus<String>>().apply {
        value = PlayStatus.Player1Playing()
    }


    fun play(player: String, choice: String) {
        Log.d(TAG, "play: "+player+" "+choice)
        if (choice.isNullOrEmpty()){
            playResult.value = PlayStatus.NoInput()
        }else {
            when (player) {
                JUGADOR_1 -> {
                    if (turn == JUGADOR_1) {
                        p1.choice = choice
                        turn = JUGADOR_2
                        playResult.value = PlayStatus.Player2Playing()
                    }

                }
                JUGADOR_2 -> {
                    if (turn == JUGADOR_2) {
                        p2.choice = choice
                        turn = JUGADOR_1
                        hasRoundFinsish = true
                    }
                }
            }
            if (hasRoundFinsish) {
                if (p1.choice == p2.choice) {
                    playResult.value = PlayStatus.GameTie()
                }

                if (game.isWin(p1, p2)) {
                    playResult.value = PlayStatus.GameWon("${p1.choice} Gana a ${p2.choice}")
                    p1.incrementRoundsWon()
                    restartRound()
                } else {
                    playResult.value = PlayStatus.GameWon("${p2.choice} Gana a ${p1.choice}")
                    p2.incrementRoundsWon()
                    restartRound()
                }

                if (p1.roundsWon == 2) {
                    playResult.value = PlayStatus.GameFinished("${p1.name} Gana!")
                } else if (p2.roundsWon == 2) {
                    playResult.value = PlayStatus.GameFinished("${p2.name} Gana!")
                }
            }
        }

    }
    fun restartRound(){
        turn = JUGADOR_1
        playResult.value = PlayStatus.Player1Playing()
        hasRoundFinsish = false
    }

    fun getPlayerChoice(player:String):String{
        when (player) {
            JUGADOR_1 -> {
                return p1.choice

            }
            JUGADOR_2 -> {
                return p2.choice
            }
        }
        return ""
    }


}