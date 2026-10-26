package cn.edu.sicnucs.bml.homework4_mul_activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Activity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_1)
        title = getString(R.string.act1_title)

        val tvParam = findViewById<TextView>(R.id.tv_param)
        val param = intent.getStringExtra(getString(R.string.extra_param_key))
            ?: getString(R.string.no_parameter_received)
        tvParam.text = getString(R.string.received_parameter, param)
    }
}
