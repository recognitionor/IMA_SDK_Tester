package com.jhlee.ima_sdk_tester

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class TestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}