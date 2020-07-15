package com.test.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var planBtn: Button? = null
    private var addonBtn: Button? = null
    private var tokenizeBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.addonBtn = findViewById(R.id.addon_btn)
        this.planBtn = findViewById(R.id.plan_btn)
        this.tokenizeBtn = findViewById(R.id.tokenize_btn)
        initializeListeners()
    }

    private fun initializeListeners() {
        this.addonBtn?.setOnClickListener {
            val intent = Intent(this, AddonActivity::class.java)
            startActivity(intent)
        }
        this.planBtn?.setOnClickListener {
            val intent = Intent(this, PlanInJavaActivity::class.java)
            startActivity(intent)
        }
        this.tokenizeBtn?.setOnClickListener {
            val intent = Intent(this, TokenizeActivity::class.java)
            startActivity(intent)
        }
    }
}