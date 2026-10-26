package cn.edu.sicnucs.bml.homework4_mul_activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class Activity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)
        title = getString(R.string.act2_title)

        val etInput = findViewById<EditText>(R.id.et_input)
        findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            val inputStr = etInput.text.toString()
            val resultIntent = Intent().apply {
                putExtra(getString(R.string.extra_return_key), inputStr)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
