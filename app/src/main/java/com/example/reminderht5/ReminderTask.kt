package com.example.reminderht5

data class ReminderTask(
    val taskId: Int = 1,
    val enabled: Boolean = false,

    val name: String = "Nhiệm vụ",

    // thời điểm hẹn đầu tiên
    val year: Int = 2026,
    val month: Int = 1,   // 1..12
    val day: Int = 1,     // 1..31
    val hour: Int = 7,    // 0..23
    val minute: Int = 0,  // 0..59

    // lặp lại
    val repeatValue: Int = 1,
    val repeatUnit: String = "Tháng", // "Giờ" "Ngày" "Tuần" "Tháng" "Năm"

    // reo lại
    val ringCount: Int = 10,
    val ringEveryMinutes: Int = 1,

    // nhạc chuông do user chọn (content://...)
    val musicUriStr: String = ""
)
