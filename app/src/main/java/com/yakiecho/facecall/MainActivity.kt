package com.yakiecho.facecall

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.CallLog
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val phoneNumberInput = findViewById<EditText>(R.id.phoneNumberInput)
        val callTypeSpinner = findViewById<Spinner>(R.id.callTypeSpinner)
        val dateTimeText = findViewById<TextView>(R.id.dateTimeText)
        val durationInput = findViewById<EditText>(R.id.durationInput)
        val selectDateTimeButton = findViewById<Button>(R.id.selectDateTimeButton)
        val addButton = findViewById<Button>(R.id.addButton)

        val calendar = Calendar.getInstance()

        selectDateTimeButton.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                TimePickerDialog(this, { _, hourOfDay, minute ->
                    calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                    dateTimeText.text = "${year}-${month + 1}-${dayOfMonth} ${hourOfDay}:${minute}"
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        addButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString()
            val callType = when (callTypeSpinner.selectedItem.toString()) {
                "Исходящий" -> CallLog.Calls.OUTGOING_TYPE
                "Входящий" -> CallLog.Calls.INCOMING_TYPE
                "Без ответа" -> CallLog.Calls.MISSED_TYPE
                else -> -1
            }

            val timestamp = calendar.timeInMillis
            val duration = durationInput.text.toString().toIntOrNull() ?: 0

            if (callType != -1) {
                addFakeCallLog(phoneNumber, callType, timestamp, duration)
            } else {
                Toast.makeText(this, "Некорректные данные", Toast.LENGTH_SHORT).show()
            }
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_CALL_LOG),
                1
            )
        }
    }

    private fun addFakeCallLog(phoneNumber: String, callType: Int, timestamp: Long, duration: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            val values = ContentValues().apply {
                put(CallLog.Calls.NUMBER, phoneNumber)
                put(CallLog.Calls.TYPE, callType)
                put(CallLog.Calls.DATE, timestamp)
                put(CallLog.Calls.DURATION, duration)
                put(CallLog.Calls.NEW, 1)
            }

            contentResolver.insert(CallLog.Calls.CONTENT_URI, values)
            Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Нет разрешения на запись в журнал вызовов", Toast.LENGTH_SHORT).show()
        }
    }
}

