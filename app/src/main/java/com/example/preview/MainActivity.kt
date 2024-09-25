package com.example.preview

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.preview.ui.theme.CountDownViewModel
import com.example.preview.ui.theme.PreviewTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val Tag = "Coroutines"
            val timerViewModel = viewModel<CountDownViewModel>()
            val timer = timerViewModel.countDownFlow.collectAsState(initial = 10)
            val counter = timerViewModel.stateFlow.collectAsState(initial = 10)
            PreviewTheme {
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center){
//                    Text(
//                        text = timer.value.toString(),
//                        fontSize = 40.sp,
//                        modifier = Modifier.align(Alignment.Center))
                    Button(onClick = {timerViewModel.incrementCounter()}) {
                        Text(text = counter.value.toString())
                    }
                }
                LaunchedEffect(key1 = true) {
                    // Collecting first
                    launch {
                        timerViewModel.sharedFlow.collect {
                            Log.d("flows", "The second timer has value $it")
                        }
                    }
                    // Emit a value after the collector is set up
                    delay(2000L)
                    timerViewModel.squareNumber(4)
                }


            }
        }
    }


    //derivedStateOf
    //derivedStateOf is basically mutableState of. In this example, when we don't use derivedStateOf of and instead use mutable state of,
    // when the text composable accesses counter, the string is concatenated again which is not needed since the value hasn't changed.
    // therefore we use derivedStateOf of which caches the value and gives that value to avoid unnecessary computations to improve system resource management.
    // When the value does change only then does the computation of concatenation does happen wth updated value which is then cached again ,
    // and derivedState behave like mutableState and notify all the UI components to recompose and the cycle continues.
    //useful for complex calculations which doesnt have to be recomputed everytime the value is accessed by UI elements
    @Composable
    fun proxyForVariables(){
        var counter by remember{
            mutableStateOf(0)
        }
        val counterText by remember {
            derivedStateOf {
                "The counter is $counter"
            }
        }
        Button(onClick = { counter++ }){
            Text(text = counterText)
        }
    }



    //rememberUpdatedState
    //Using LaunchedEffect with key = true makes it so that it gets launched only once and the code
    // inside doesnt get relaunched during recomposition. however the onTimeOut function may get updated
    // in the activity lifecycle, and we want the code inside lanchedEffect to rememer that but we dont want
    // it to get executed again by using ONtimeout as key, so we instead use rememberUpdatedState so that the
    // updatedState of the function is remembered but no recompositoin or relaunching of coroutine happens.
    // Its basically rememberMutableState without the atutomatic updation of UI due to changes in value
    @Composable
    fun updateTimerValue(
        onTimeOut:() -> Unit
    ){
        val updatedOnTimeOut by rememberUpdatedState(newValue = onTimeOut)
        LaunchedEffect(key1 = true) {
            updatedOnTimeOut()
        }
    }


    // DisposableEffect
    //So to summarise, LaunchedEffect and DisposableEffect have same core functionality -
    // Key which if changed causes the coroutine to get launched again,
    // both survive recompositions. However if due to configuration changes or that particular UI element gets destroyed,
    // the coroutine gets launched again in both cases. What makes disposeEffect special is that
    // when the destroy stage is reached, we can provide it a function or code to be executed in the onDispose block,
    // which usually contains deleting certain active elements to prevent memory leaks
    @Composable
    fun cleanUpByCoroutine(){
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver{_,event ->
                if(event == Lifecycle.Event.ON_PAUSE){
                    println("OnPause called")
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    //SideEffect
    //called after every recompostion. Different from LaunchedEffect and DisposeEffect in that it cant survive recompostions
    //while LaunchedEffect and DisposeEffect can survive recompositoins
    // So instead of SideEffect we can could just put the code inside the composable itself so that it automatically
    // gets re-executed for each recomposition. except it is useful for functionality like executing network calls which needs
    // to be done asynchronously otherwise the main thread is blocked
    @Composable
    fun mutableStateSimulator(nonComposeEntity:String){
        //nonComposeEntity could be something which cant be used as mutablestate for some reason like
        // - Its fetched from API Call, third party library
        SideEffect {
            //updation code of nonComposeEntity for every recomposition
        }
    }

//
//        @Composable
//        fun aboutUsPage() {
//            val sfPro =
//                FontFamily(
//                    Font(R.font.sfpro_thin, FontWeight.Medium),
//                )
//
//            val configuration = LocalConfiguration.current
//            val screenWidth = configuration.screenWidthDp.dp
//
//            Box(
//                modifier = Modifier.fillMaxWidth().fillMaxHeight(1f).background(color = Color.Black),
//                contentAlignment = Alignment.TopCenter,
//            ) {
//                Column {
//                    Spacer(modifier = Modifier.fillMaxHeight(0.15f))
//                    Row(modifier = Modifier.fillMaxWidth()) {
//                        Image(
//                            painter = painterResource(R.drawable.about_image),
//                            modifier = Modifier.scale(scaleY = 2.2f, scaleX = 2f).padding(horizontal = 10.dp),
//                            contentDescription = "",
//                        )
//                    }
//                }
//
//                Image(
//                    painter = painterResource(R.drawable.about_basebckg),
//                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
//                    contentDescription = "",
//                )
//
//                Column {
//                    Spacer(modifier = Modifier.fillMaxHeight(0.08f))
//                    Row(modifier = Modifier.fillMaxWidth(),){
//                        Spacer(modifier = Modifier.fillMaxWidth(0.02f))
//                        Image(
//                            painter = painterResource(R.drawable.about_text),
//                            modifier = Modifier.scale(scaleY = 0.85f, scaleX = 0.95f),
//                            contentDescription = "",
//                        )
//                    }
//
//                }
//
//                Column {
//                    Spacer(modifier = Modifier.fillMaxHeight(0.25f))
//                    Row {
//                        Spacer(modifier = Modifier.fillMaxWidth(0.75f))
//                        Image(
//                            painter = painterResource(R.drawable.about_indianculture),
//                            modifier = Modifier.scale(scaleY = 2.5f, scaleX = 2f),
//                            contentDescription = "",
//                        )
//                    }
//
//                }
//
//                Column() {
//                    Spacer(modifier = Modifier.fillMaxHeight(0.47f))
//                    Row(modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Start) {
//                        Image(
//                            painter = painterResource(R.drawable.about_madeinindia),
//                            modifier = Modifier.scale(scaleY = 2.5f, scaleX = 2f).offset(x = 16.dp),
//                            contentDescription = "",
//                        )
//                    }
//                }
//
//                Column {
//                    Spacer(modifier = Modifier.weight(1.4f))
//                    Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
//                        Text(
//                            style = TextStyle(fontFamily = sfPro, fontSize = 19.sp),
//                            modifier = Modifier.padding(10.dp).fillMaxHeight(0.6f),
//                            color = Color.White,
//                            text =
//                            """
//                                Festember is is NIT Trichy's vibrant cultural spectacle, entirely driven by student passion and innovation. With an impressive annual footfall of 18,000 students from 500 colleges across India, it stands as one of the most awaited fests in the country. Featuring an extensive lineup of events spanning 12 diverse clusters, from Music to Fashion, Arts to Gaming, and more, Festember is a platform where participants compete for prestigious cash prizes. Join us as we continue to create unforgettable moments and nurture creative talents.
//                                """.trimIndent(),
//                            textAlign = TextAlign.Justify,
//                        )
//                    }
//                    Spacer(modifier = Modifier.weight(0.3f))
//                }
//            }
//        }
//
//    @Preview(showBackground = true)
//    @Composable
//    fun abutUsPagePreview(){
//        aboutUsPage()
//    }
}