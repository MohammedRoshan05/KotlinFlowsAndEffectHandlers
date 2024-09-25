package com.example.preview.ui.theme

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CountDownViewModel:ViewModel() {
    val countDownFlow = flow<Int> {
        val startingValue = 10
        var currentTime = startingValue
        emit(currentTime)
        while(currentTime > 0){
            delay(1000L)
            currentTime--
            emit(currentTime)
        }
    }

    private val _stateFlow = MutableStateFlow(0)
    val stateFlow = _stateFlow.asStateFlow()

    fun incrementCounter(){
        _stateFlow.value += 1
    }

    private val _sharedFlow = MutableSharedFlow<Int>()
    val sharedFlow = _sharedFlow.asSharedFlow()

    fun squareNumber(number:Int){
        viewModelScope.launch {
            _sharedFlow.emit(number * number)
        }
    }

    init {
//        collectValues()
//        viewModelScope.launch {
//            sharedFlow.collect(){
//                delay(1000L)
//                Log.d("flows","The first timer has value $it")
//            }
//        }
//
//        viewModelScope.launch {
//            sharedFlow.collect(){
//                delay(1000L)
//                Log.d("flows","The second timer has value $it")
//            }
//        }
//        squareNumber(3)
    }
    private fun collectValues(){
        viewModelScope.launch {

            //after each emission, immediatey compiler jumps to collect block, then after collect block is finished
            // it goes bcak to next line of flow
//            countDownFlow
//                .filter { it % 2 == 0 }
//                .map { it * it } //transforms each element of flow
//                .collect(){time ->
//                Log.d("flows","The timer has value $time")
//            }

            //terminal operators -> count,reduce, fold
//            val count = countDownFlow
//                .filter { it % 2 == 0 }
//                .map { it + it }
//                .count(){
//                    it % 2 == 0
//                }
//            Log.d("flows","The timer has value $count")

            //flattening commands(joining two flows?)
            //flatMapConcat => flow1 emits 1, then it immediately goes to flatmapconcat command and prints 2 in log, then
            //returns back to flow, has delay of 1 second and emits 2, which then gets printed as 3 in logcat
            //the two flows dont run asynchronously
            //flatMapLatest => Same concept as collectLatest. This runs asynchronously, and if second emission reaches
            // the second flow before first emission completes executoin in second flow, first emission gets dropped
//            val flow1 = flow {
//                emit(1)
//                delay(1000L)
//                emit(2)
//            }
//            flow1.flatMapConcat {
//                flow {
//                    emit(it + 1)
//                }
//            }.collect{value ->
//                Log.d("flows","The emission is $value")
//            }


            //buffer
            val flow1 = flow {
                delay(500L)
                emit("Appettizer")
                delay(1000L)
                emit("MainDish")
                delay(250L)
                emit("Desert")
            }


            //buffer() makes the collect block run in a seperate coroutine to the flow, thus they both are aysnchronous
            //conflate does the same except if two emissions are there to be completed i.e. to be executed in
            // collect block while earlier emission is being executed,
            // it only completes the latest emission in the collect block
            // lets say two emissions are to be completed in the collect block, buffer makes them enter collect block in sequence,
            // while conflate drops earlier emissions and only colects latest emission
            flow1.onEach {
                Log.d("flows","ordered $it")
            }
//                .buffer()
                .conflate()
//                .collect()
                .collectLatest{
                    Log.d("flows","eating $it")
                    delay(1500L)
                    Log.d("flows","finished $it")

                }
            //when i used collect with conflate, an earlier emission is being executed in collect block while two new emissions are present,
            // so it finishes earlier emission, drops earlier of the two new emissions and does only the latest emission,
            // while collectLatest will discard the first emission entirely in the collect block and go execute the third emission
        }
    }
}