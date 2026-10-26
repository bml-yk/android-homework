package cn.edu.sicnucs.bml.experiment2_mvc

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.edu.sicnucs.bml.experiment2_mvc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // 1. 声明 ViewBinding
    private lateinit var binding: ActivityMainBinding
    // 2. 实例化 Model 层
    private val model = ProgramAdviserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. 使用 ViewBinding 初始化布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 保留系统默认边距处理
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- 功能1：区1，动态添加 TextView ---
        binding.btnAdd.setOnClickListener {
            // 动态创建 TextView
            val newTextView = TextView(this).apply {
                // 必须使用字符串资源
                text = getString(R.string.dynamic_text_content)
                textSize = 18f
                setPadding(20, 20, 20, 20)
            }
            // 将 TextView 添加到 ScrollView 内的 LinearLayout 中
            binding.containerLayout.addView(newTextView)
        }

        // --- 功能2：区2，MVC 查询交互 ---
        binding.btnQuery.setOnClickListener {
            // 获取输入框内容
            val inputText = binding.etInput.text.toString()
            // 调用 Model 层获取结果 (MVC 模式)
            val result = model.queryData(inputText)
            // 更新 View 层显示结果
            binding.tvResult.text = result
        }
    }
}