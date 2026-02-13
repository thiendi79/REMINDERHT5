package com.example.reminderht5

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun ReminderHomeScreen() {
    val context = LocalContext.current
    val scroll = rememberScrollState()

    // ====== STATE (demo) ======
    var powerOn by remember { mutableStateOf(true) }

    var taskName by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    var repeatValue by remember { mutableStateOf("1") }
    var repeatUnit by remember { mutableStateOf("Tháng") }

    var alarmTimes by remember { mutableStateOf("10") }
    var alarmGapMinutes by remember { mutableStateOf("5") }

    var selectedDateText by remember { mutableStateOf("Chưa chọn ngày") }
    var selectedTimeText by remember { mutableStateOf("Chưa chọn giờ") }

    var musicText by remember { mutableStateOf("Mặc định") }

    // ====== DIALOGS ======
    fun pickDate() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, y, m, d ->
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                selectedDateText = "$dd/$mm/$y"
            },
            year, month, day
        ).show()
    }

    fun pickTime() {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, h, min ->
                val hh = h.toString().padStart(2, '0')
                val mm = min.toString().padStart(2, '0')
                selectedTimeText = "$hh:$mm"
            },
            hour, minute, true
        ).show()
    }

    // ====== UI ======
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "REMINDER HT",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.weight(1f))
            Text(text = if (powerOn) "BẬT" else "TẮT", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Switch(checked = powerOn, onCheckedChange = { powerOn = it })
        }

        Spacer(Modifier.height(10.dp))

        // Mode buttons row (3 nút như kiểu “tab”)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = { Toast.makeText(context, "TAB 1", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) { Text("CÀI") }

            FilledTonalButton(
                onClick = { Toast.makeText(context, "TAB 2", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) { Text("LỊCH") }

            FilledTonalButton(
                onClick = { Toast.makeText(context, "TAB 3", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) { Text("NHẮC") }
        }

        Spacer(Modifier.height(12.dp))

        // Top action row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { powerOn = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                shape = RoundedCornerShape(10.dp)
            ) { Text("TẠM DỪNG", color = Color.White, fontWeight = FontWeight.Bold) }

            Button(
                onClick = { powerOn = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(10.dp)
            ) { Text("ĐỒNG HÀNH", color = Color.White, fontWeight = FontWeight.Bold) }
        }

        Spacer(Modifier.height(12.dp))

        // CONTROL CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E5E5))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "BẢNG ĐIỀU KHIỂN",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tên nhiệm vụ") },
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ghi chú") }
                )

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // Date + Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { pickDate() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.width(140.dp)
                    ) { Text("CHỌN NGÀY") }

                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = selectedDateText,
                        color = Color(0xFF555555),
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { pickTime() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.width(140.dp)
                    ) { Text("CHỌN GIỜ") }

                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = selectedTimeText,
                        color = Color(0xFF555555),
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // Repeat row
                Text(text = "Chu kỳ lặp", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = repeatValue,
                        onValueChange = { repeatValue = it.filter { ch -> ch.isDigit() }.ifEmpty { "1" } },
                        modifier = Modifier.width(110.dp),
                        label = { Text("Số") },
                        singleLine = true
                    )

                    Spacer(Modifier.width(10.dp))

                    UnitDropdown(
                        current = repeatUnit,
                        onChange = { repeatUnit = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // Alarm config
                Text(text = "Báo lại", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = alarmTimes,
                        onValueChange = { alarmTimes = it.filter { ch -> ch.isDigit() }.ifEmpty { "1" } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Số lần") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = alarmGapMinutes,
                        onValueChange = { alarmGapMinutes = it.filter { ch -> ch.isDigit() }.ifEmpty { "1" } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Cách nhau (phút)") },
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // Music row
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Nhạc:", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Text(text = musicText, color = Color(0xFF444444))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { musicText = "Mặc định" }) { Text("ĐỔI") }
                }

                Spacer(Modifier.height(14.dp))

                // Bottom big action button
                Button(
                    onClick = {
                        Toast.makeText(context, "ĐÃ LƯU (demo)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (powerOn) "✓ KÍCH HOẠT NHẮC VIỆC" else "✓ LƯU (ĐANG TẮT)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, "XEM NHẬT KÝ (demo)", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("XEM NHẬT KÝ", color = Color.White, fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = { Toast.makeText(context, "XÓA TẤT CẢ (demo)", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("XÓA TẤT CẢ", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Gợi ý: Nếu bạn muốn y hệt bản sáng (nhiều khối và nút hơn), chụp màn hình bản sáng hoặc nói tên từng nút bạn muốn, mình sẽ làm đúng 1:1.",
            color = Color(0xFF666666),
            fontSize = 13.sp,
            textAlign = TextAlign.Start
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun UnitDropdown(
    current: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Ngày", "Tuần", "Tháng", "Năm")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(10.dp))
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp)
                .border(0.dp, Color.Transparent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = current, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Text(text = "▼", color = Color(0xFF666666))
            }
        }

        // Click layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .padding(0.dp)
        ) {
            // dùng TextButton vô hình để click dễ
            TextButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxSize()
            ) {}
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onChange(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
