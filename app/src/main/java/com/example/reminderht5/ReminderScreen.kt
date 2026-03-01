package com.example.reminderht5

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import java.util.Calendar

@Composable
fun ReminderScreen() {
    ReminderHomeScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderHomeScreen() {
    val ctx = LocalContext.current

    var masterPower by rememberSaveable { mutableStateOf(true) }

    var taskName by rememberSaveable { mutableStateOf("") }
    var repeatValue by rememberSaveable { mutableStateOf("1") }
    var repeatUnit by rememberSaveable { mutableStateOf("Tháng") }
    var ringCount by rememberSaveable { mutableStateOf("10") }
    var ringEveryMinutes by rememberSaveable { mutableStateOf("1") }

    var timeText by rememberSaveable { mutableStateOf("Chưa thiết lập") }
    var pickedYear by rememberSaveable { mutableStateOf(2026) }
    var pickedMonth by rememberSaveable { mutableStateOf(1) }
    var pickedDay by rememberSaveable { mutableStateOf(1) }
    var pickedHour by rememberSaveable { mutableStateOf(7) }
    var pickedMinute by rememberSaveable { mutableStateOf(0) }

    var musicUriStr by rememberSaveable { mutableStateOf("") }
    var musicLabel by rememberSaveable { mutableStateOf("Mặc định") }

    // ====== Google Sign-In for Sync ======
    var showSyncDialog by rememberSaveable { mutableStateOf(false) }
    var signedUid by rememberSaveable { mutableStateOf(GoogleAuth.currentUid() ?: "") }

    // ====== Delete all confirm (Trang 2) ======
    var showDeleteAllConfirm by rememberSaveable { mutableStateOf(false) }

    // ====== Load task for display ======
    var currentTask by remember { mutableStateOf(TaskPrefs.loadTask(ctx)) }

    // ====== Pager 2 trang ======
    val pagerState = rememberPagerState(initialPage = 0)

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        GoogleAuth.handleSignInResult(
            data = res.data,
            onSuccess = { uid ->
                signedUid = uid
                toast(ctx, "Đăng nhập OK")
                showSyncDialog = true
            },
            onError = { msg ->
                toast(ctx, msg)
            }
        )
    }

    // ====== Music picker ======
    val pickMusicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            toast(ctx, "Bạn chưa chọn nhạc")
            return@rememberLauncherForActivityResult
        }

        try {
            ctx.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }

        musicUriStr = uri.toString()
        musicLabel = resolveDisplayName(ctx, uri) ?: "Đã chọn"
        toast(ctx, "Đã chọn nhạc: $musicLabel")
    }

    fun pickDateThenTime() {
        val cal = Calendar.getInstance()

        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                pickedYear = y
                pickedMonth = m + 1
                pickedDay = d

                TimePickerDialog(
                    ctx,
                    { _, hh, mm ->
                        pickedHour = hh
                        pickedMinute = mm
                        timeText = "%02d/%02d/%04d %02d:%02d".format(
                            pickedDay, pickedMonth, pickedYear, pickedHour, pickedMinute
                        )
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ====== Dropdown repeat unit ======
    val units = listOf("Ngày", "Tháng", "Năm", "Giờ", "Phút")
    var unitExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        NotificationHelper.ensureChannels(ctx)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("REMINDERHT5", fontWeight = FontWeight.ExtraBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior(
                    rememberTopAppBarState()
                )
            )
        }
    ) { padding ->

        HorizontalPager(
            count = 2,
            state = pagerState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->

            when (page) {

                // =========================
                // TRANG 1: chỉ tới "KÍCH HOẠT NHIỆM VỤ MỚI"
                // =========================
                0 -> {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                            .navigationBarsPadding()
                            .imePadding()
                    ) {

                        // Đồng bộ
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val uid = GoogleAuth.currentUid()
                                if (uid.isNullOrBlank()) {
                                    googleSignInLauncher.launch(GoogleAuth.getSignInIntent(ctx))
                                } else {
                                    signedUid = uid
                                    showSyncDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                        ) {
                            Text("ĐỒNG BỘ", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(8.dp))

                        // Chọn nhạc chuông
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { pickMusicLauncher.launch(arrayOf("audio/*")) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                            ) {
                                Text("CHỌN NHẠC CHUÔNG", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(musicLabel)
                        }

                        Spacer(Modifier.height(8.dp))

                        // Hai nút Tạm dừng / Dừng hẳn
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    AlarmSoundService.stop(ctx)
                                    toast(ctx, "Tạm dừng")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1C40F))
                            ) {
                                Text("TẠM DỪNG", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val t = TaskPrefs.loadTask(ctx)
                                    AlarmSoundService.stop(ctx)
                                    AlarmScheduler.cancelAll(ctx, t.taskId)
                                    TaskPrefs.setEnabled(ctx, false)
                                    toast(ctx, "Đã dừng hẳn")
                                    currentTask = TaskPrefs.loadTask(ctx)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                            ) {
                                Text("DỪNG HẲN", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Bảng điều khiển
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "BẢNG ĐIỀU KHIỂN",
                                    color = Color(0xFF2ECC71),
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEAECEE))
                                        .padding(8.dp)
                                )

                                Spacer(Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = taskName,
                                    onValueChange = { taskName = it },
                                    label = { Text("Tên nhiệm vụ") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))

                                // chọn giờ
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { pickDateThenTime() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                                    ) {
                                        Text("CHỌN GIỜ", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(timeText)
                                }

                                Spacer(Modifier.height(10.dp))

                                Text("Lặp lại mỗi", color = Color.Gray)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = repeatValue,
                                        onValueChange = { repeatValue = it.filter { ch -> ch.isDigit() }.ifBlank { "" } },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { unitExpanded = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(repeatUnit)
                                        }
                                        DropdownMenu(
                                            expanded = unitExpanded,
                                            onDismissRequest = { unitExpanded = false }
                                        ) {
                                            units.forEach { u ->
                                                DropdownMenuItem(
                                                    text = { Text(u) },
                                                    onClick = {
                                                        repeatUnit = u
                                                        unitExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Text("Số lần báo thức | Cách phút:", color = Color.Gray)

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = ringCount,
                                        onValueChange = { ringCount = it.filter { ch -> ch.isDigit() }.ifBlank { "" } },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = ringEveryMinutes,
                                        onValueChange = { ringEveryMinutes = it.filter { ch -> ch.isDigit() }.ifBlank { "" } },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                // Kích hoạt nhiệm vụ mới (NÚT CUỐI TRANG 1)
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        val rv = repeatValue.toIntOrNull() ?: 1
                                        val rc = ringCount.toIntOrNull() ?: 10
                                        val rem = ringEveryMinutes.toIntOrNull() ?: 1

                                        val t = ReminderTask(
                                            taskId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                                            enabled = true,
                                            name = taskName.ifBlank { "Nhiệm vụ" },
                                            year = pickedYear,
                                            month = pickedMonth,
                                            day = pickedDay,
                                            hour = pickedHour,
                                            minute = pickedMinute,
                                            repeatValue = rv,
                                            repeatUnit = repeatUnit,
                                            ringCount = rc,
                                            ringEveryMinutes = rem,
                                            musicUriStr = musicUriStr
                                        )

                                        TaskPrefs.saveTask(ctx, t)
                                        TaskPrefs.setEnabled(ctx, true)

                                        AlarmScheduler.scheduleBase(ctx, t)

                                        toast(ctx, "Đã kích hoạt nhiệm vụ mới")
                                        timeText = "Chưa thiết lập"
                                        taskName = ""
                                        currentTask = TaskPrefs.loadTask(ctx)
                                    },
                                    enabled = masterPower,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                                ) {
                                    Text("KÍCH HOẠT NHIỆM VỤ MỚI", fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        // chừa thêm đáy để chắc chắn không bị thanh hệ thống che
                        Spacer(Modifier.height(24.dp))
                    }
                }

                // =========================
                // TRANG 2: hồ sơ + xoá + danh sách cuộn dài
                // =========================
                1 -> {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Folder, contentDescription = "Folder")
                            Spacer(Modifier.width(8.dp))
                            Text("HỒ SƠ ĐANG CHẠY", fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = { showDeleteAllConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                            ) {
                                Text("XOÁ TẤT CẢ", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(Modifier.padding(vertical = 8.dp))

                        // Hồ sơ đang chạy (hiện tại app bạn đang lưu 1 nhiệm vụ)
                        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            val t = currentTask
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Tên: ${t.name}")
                                Text("Bật: ${t.enabled}")
                                Text("Giờ: ${t.month}/${t.day} ${t.hour}:${t.minute}")
                                Text("Lặp: ${t.repeatValue} ${t.repeatUnit}")
                                Text("Reo: ${t.ringCount} lần / ${t.ringEveryMinutes} phút")
                                Text("Nhạc: ${if (t.musicUriStr.isBlank()) "Mặc định" else "Đã chọn"}")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Chỗ này sau này bạn thay bằng danh sách dài (cuộn vô hạn)
                        Text(
                            "Danh sách dài sẽ đặt ở đây (cuộn vô hạn).",
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // ====== Sync Dialog ======
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = { Text("ĐỒNG BỘ") },
            text = { Text("Chọn thao tác đồng bộ với Cloud") },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = signedUid.ifBlank { GoogleAuth.currentUid().orEmpty() }
                        if (uid.isBlank()) {
                            toast(ctx, "Chưa đăng nhập")
                            return@Button
                        }
                        CloudSync.uploadTask(ctx, uid) { _, msg ->
                            toast(ctx, msg)
                            showSyncDialog = false
                        }
                    }
                ) { Text("TẢI LÊN CLOUD") }
            },
            dismissButton = {
                Row {
                    OutlinedButton(
                        onClick = {
                            val uid = signedUid.ifBlank { GoogleAuth.currentUid().orEmpty() }
                            if (uid.isBlank()) {
                                toast(ctx, "Chưa đăng nhập")
                                return@OutlinedButton
                            }
                            CloudSync.downloadTask(ctx, uid) { _, msg ->
                                toast(ctx, msg)
                                currentTask = TaskPrefs.loadTask(ctx)
                                showSyncDialog = false
                            }
                        }
                    ) { Text("TẢI VỀ MÁY") }

                    Spacer(Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            GoogleAuth.signOut(ctx) {
                                signedUid = ""
                                toast(ctx, "Đã đăng xuất")
                                showSyncDialog = false
                            }
                        }
                    ) { Text("ĐĂNG XUẤT") }
                }
            }
        )
    }

    // ====== Confirm delete all (Trang 2) ======
    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text("Xoá tất cả?") },
            text = { Text("Bạn có chắc muốn xoá toàn bộ hồ sơ nhiệm vụ?") },
            confirmButton = {
                Button(
                    onClick = {
                        val t = TaskPrefs.loadTask(ctx)
                        AlarmSoundService.stop(ctx)
                        AlarmScheduler.cancelAll(ctx, t.taskId)
                        TaskPrefs.clear(ctx)
                        toast(ctx, "Đã xoá tất cả")
                        currentTask = TaskPrefs.loadTask(ctx)
                        showDeleteAllConfirm = false
                    }
                ) { Text("Xác nhận") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAllConfirm = false }) { Text("Huỷ") }
            }
        )
    }
}

private fun toast(ctx: Context, msg: String) {
    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
}

private fun resolveDisplayName(ctx: Context, uri: Uri): String? {
    return try {
        ctx.contentResolver.query(uri, null, null, null, null)?.use { cur ->
            val nameIndex = cur.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cur.moveToFirst()) cur.getString(nameIndex) else null
        }
    } catch (_: Exception) {
        null
    }
}

private fun Context.findActivity(): ComponentActivity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is ComponentActivity) return c
        c = c.baseContext
    }
    return null
}