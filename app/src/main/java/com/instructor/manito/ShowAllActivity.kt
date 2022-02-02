package com.instructor.manito

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.instructor.manito.databinding.ActivityShowAllBinding

class ShowAllActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityShowAllBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        with(bind){
            showAllPrevButton.setOnClickListener{
                val intent = Intent(this@ShowAllActivity,FinishActivity::class.java)
                startActivity(intent)
            }
        }
    }
}