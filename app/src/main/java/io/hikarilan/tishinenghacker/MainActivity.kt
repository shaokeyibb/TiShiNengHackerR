package io.hikarilan.tishinenghacker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.hikarilan.tishinenghacker.ui.theme.Purple80
import io.hikarilan.tishinenghacker.ui.theme.PurpleGrey40
import io.hikarilan.tishinenghacker.ui.theme.TiShiNengHackerRTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class MainActivity : ComponentActivity() {

    private val pendingDistance = mutableStateOf("1000.0") // 1000.0
    private val pendingStep = mutableStateOf("1428") // 1428
    private val pendingTime = mutableStateOf("3600") // 3600

    private val distance = mutableStateOf(pendingDistance.value.toFloat())
    private val step = mutableStateOf(pendingStep.value.toInt())
    private val time = mutableStateOf(pendingTime.value.toLong())
    private val acceptedTerms = mutableStateOf(false)
    private val isRunning = mutableStateOf(false)
    private var runningThread: Thread? = null

    private val snackbarHostState = SnackbarHostState()

    private val core = {
        while (true) {
            if (System.currentTimeMillis() % 5 != 0L) continue
            sendBroadcast(Intent().apply {
                this.action = "TIME_CHANGE"
                this.putExtra("countTime", time.value)
            })
            sendBroadcast(Intent().apply {
                this.action = "ADD_LINE"
                this.putExtra("distance", distance.value)
            })
            sendBroadcast(Intent().apply {
                this.action = "UPDATE_STEP"
                this.putExtra("stepCount", step.value)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Main() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(showBackground = true)
    @Composable
    fun Main() {
        TiShiNengHackerRTheme {
            Scaffold(topBar = {
                TopAppBar()
            }, floatingActionButton = {
                FabButton()
            }, snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Content()
                        Divider(modifier = Modifier.padding(3.dp))
                        Footer()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBar() {
        CenterAlignedTopAppBar(
            title = {
                Text(text = "TiShiNengHackerR")
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Purple80
            ), navigationIcon = {
                if (isRunning.value) {
                    CircularProgressIndicator()
                }
            }, actions = {
                IconButton(onClick = {
                    startActivity(Intent().apply {
                        action = "android.intent.action.VIEW"
                        data = Uri.parse("https://github.com/shaokeyibb/TishiNengHackerR")
                    })
                }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "GitHub"
                    )
                }
            }
        )
    }

    @Composable
    fun FabButton() {
        ExtendedFloatingActionButton(icon = {
            if (!isRunning.value) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Run")
            } else {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Stop")
            }
        }, text = {
            if (!isRunning.value) {
                Text(text = "?????? Hacker")
            } else {
                Text(text = "?????? Hacker")
            }
        }, onClick = { onClickFabBtn() })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, end = 5.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                Text(text = "??????????????????????????????????????????")
                OutlinedTextField(
                    value = pendingDistance.value,
                    onValueChange = {
                        pendingDistance.value = it
                        if (checkPendingDistance(it)) {
                            distance.value = it.toFloat()
                            step.value = (distance.value / 0.7).toInt()
                            pendingStep.value = step.value.toString()
                        }
                    },
                    singleLine = true,
                    placeholder = { Text(text = "1000.0") },
                    supportingText = { Text(text = "???????????????????????????????????????????????????????????????????????????????????????????????????") },
                    label = { Text(text = "??????????????????") },
                    isError = !checkPendingDistance(pendingDistance.value),
                    enabled = !isRunning.value
                )
            }
            Column(
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                Text(text = "??????????????????????????????????????????????????????")
                OutlinedTextField(
                    value = pendingStep.value,
                    onValueChange = {
                        pendingStep.value = it
                        if (checkPendingStep(it)) {
                            step.value = it.toInt()
                        }
                    },
                    singleLine = true,
                    placeholder = { Text(text = "1428") },
                    supportingText = { Text(text = "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????") },
                    label = { Text(text = "??????????????????") },
                    isError = !checkPendingStep(pendingStep.value),
                    enabled = !isRunning.value
                )
            }
            Column(
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                Text(text = "???????????????????????????????????????")
                OutlinedTextField(
                    value = pendingTime.value,
                    onValueChange = {
                        pendingTime.value = it
                        if (checkPendingTime(it)) {
                            time.value = it.toDouble().roundToLong()
                        }
                    },
                    singleLine = true,
                    placeholder = { Text(text = "3600") },
                    supportingText = { Text(text = "???????????????????????????????????????") },
                    label = { Text(text = "??????????????????") },
                    isError = !checkPendingTime(pendingTime.value),
                    enabled = !isRunning.value
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "????????????????????????????????????????????????")
                    Switch(
                        checked = acceptedTerms.value,
                        onCheckedChange = { acceptedTerms.value = true },
                        enabled = !isRunning.value
                    )
                }
            }
            Text(text = "????????????????????????????????? Hacker ???????????????????????????")
            Text(
                text = "????????? ?????????${distance.value}M????????????${step.value}???????????????${time.value}???",
                color = PurpleGrey40
            )
            Text(text = "???????????????" + if (isRunning.value) "?????????" else "????????????", color = PurpleGrey40)
        }
    }

    @Composable
    fun Footer() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "TiShiNengHackerR ????????? GNU General Public License v3.0 ???????????????????????????????????????????????????????????????????????????????????????????????? GPLv3 ?????????????????????")
            Spacer(modifier = Modifier.padding(3.dp))
            Text(
                text = "???????????????TiShiNengHackerR ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????",
                color = Color.Red
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Text(
                text = """
    TiShiNengHackerR
    Copyright (C) 2022  HikariLan
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
        """.trimIndent(),
                fontStyle = FontStyle.Italic
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun onClickFabBtn() {
        if (!acceptedTerms.value) {
            GlobalScope.launch {
                snackbarHostState.showSnackbar(
                    message = "????????????????????????",
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
            }
            return
        }
        if (!isRunning.value) {
            runHacker()
        } else {
            stopHacker()
        }
    }

    private fun runHacker() {
        isRunning.value = true
        runningThread = Thread(core).also { it.start() }
    }

    private fun stopHacker() {
        runningThread?.interrupt()
        isRunning.value = false
    }
}

fun checkPendingDistance(distance: String) = distance.toFloatOrNull() != null

fun checkPendingStep(step: String) = step.toIntOrNull() != null

fun checkPendingTime(time: String) = time.toDoubleOrNull() != null