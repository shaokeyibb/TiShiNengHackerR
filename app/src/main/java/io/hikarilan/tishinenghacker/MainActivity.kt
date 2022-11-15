package io.hikarilan.tishinenghacker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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

class MainActivity : ComponentActivity() {

    private val pendingDistance = mutableStateOf("1000.0") // 1000.0
    private val pendingStep = mutableStateOf("1428") // 1428

    private val distance = mutableStateOf(pendingDistance.value.toFloat())
    private val step = mutableStateOf(pendingStep.value.toInt())
    private val acceptedTerms = mutableStateOf(false)
    private val isRunning = mutableStateOf(false)
    private var runningThread: Thread? = null

    private val snackbarHostState = SnackbarHostState()

    private val core = {
        while (true) {
            if (System.currentTimeMillis() % 5 != 0L) continue
            sendBroadcast(Intent().apply {
                this.action = "ADD_START_POINT"
                this.putExtra(
                    "startTime", System.currentTimeMillis() - 6000000L
                )
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
                Text(text = "运行 Hacker")
            } else {
                Text(text = "停止 Hacker")
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
                Text(text = "第一步：设置本次跑步总计距离")
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
                    supportingText = { Text(text = "单位米，支持小数，请尽量设置一个较小的非整数值以免引起不必要的怀疑") },
                    label = { Text(text = "本次跑步距离") },
                    isError = !checkPendingDistance(pendingDistance.value),
                    enabled = !isRunning.value
                )
            }
            Column(
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                Text(text = "第二步：设置本次跑步总计步数（可选）")
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
                    supportingText = { Text(text = "不出意外的话，该项应当已由上一步所输入值自动计算，如果您希望指定一个自定义值，则可以更改此处") },
                    label = { Text(text = "本次跑步步数") },
                    isError = !checkPendingStep(pendingStep.value),
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
                    Text(text = "第三步：勾选以同意我们的免责声明")
                    Switch(
                        checked = acceptedTerms.value,
                        onCheckedChange = { acceptedTerms.value = true },
                        enabled = !isRunning.value
                    )
                }
            }
            Text(text = "第四步：点击下方的运行 Hacker 按钮，然后开始跑步")
            Text(text = "概览： 距离：${distance.value}M，步数：${step.value}步", color = PurpleGrey40)
            Text(text = "运行状态：" + if (isRunning.value) "运行中" else "未在运行", color = PurpleGrey40)
        }
    }

    @Composable
    fun Footer() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "TiShiNengHackerR 是基于 GNU General Public License v3.0 协议许可的自由软件。使用，修改，再分发该软件（及其源代码）须按照 GPLv3 协议要求进行。")
            Spacer(modifier = Modifier.padding(2.dp))
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
            Spacer(modifier = Modifier.padding(1.dp))
            Text(
                text = "免责声明：TiShiNengHackerR 仅用作学习和研究用途，任何滥用行为与该软件作者无关。使用本软件即代表您同意自愿承担使用该软件造成的一切可能的后果。",
                color = Color.Red
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun onClickFabBtn() {
        if (!acceptedTerms.value) {
            GlobalScope.launch {
                snackbarHostState.showSnackbar(
                    message = "请先同意免责声明",
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