package cn.edu.sicnucs.bml.homework4_mul_activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvReturnedResult: TextView

    private val act2Launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val returnedStr = result.data?.getStringExtra(getString(R.string.extra_return_key))
                ?.ifBlank { getString(R.string.empty_return_value) }
                ?: getString(R.string.empty_return_value)
            tvReturnedResult.text = getString(R.string.returned_result, returnedStr)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvReturnedResult = findViewById(R.id.tv_returned_result)

        findViewById<Button>(R.id.btn_act1).setOnClickListener {
            val intent = Intent(this, Activity1::class.java)
            intent.putExtra(getString(R.string.extra_param_key), getString(R.string.forward_parameter_value))
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_act2).setOnClickListener {
            val intent = Intent(this, Activity2::class.java)
            act2Launcher.launch(intent)
        }

        findViewById<Button>(R.id.btn_act3).setOnClickListener {
            startActivity(Intent(this, Activity3::class.java))
        }

        findViewById<Button>(R.id.btn_act4).setOnClickListener {
            startActivity(Intent(this, Activity4::class.java))
        }
    }
}
