package com.dwarsh.webrtcsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        start_meeting.setOnClickListener {
            if (meeting_id.text.toString().trim().isNullOrEmpty())
                meeting_id.error = "Please enter meeting id"
            else {
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("meetingID",meeting_id.text.toString())
                intent.putExtra("isJoin",false)
                startActivity(intent)
            }
        }
        join_meeting.setOnClickListener {
            if (meeting_id.text.toString().trim().isNullOrEmpty())
                meeting_id.error = "Please enter meeting id"
            else {
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("meetingID",meeting_id.text.toString())
                intent.putExtra("isJoin",true)
                startActivity(intent)
            }
        }
    }
}