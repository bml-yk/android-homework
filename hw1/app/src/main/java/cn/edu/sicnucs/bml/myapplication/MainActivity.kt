package cn.edu.sicnucs.bml.myapplication

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var currentLangIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ========== 1. 根布局优化：增加背景色和边距 ==========
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F5F5F5")) // 浅灰色背景
            setPadding(50, 50, 50, 50) // 给整个页面留出内边距
        }

        // ========== 2. 文本优化 ==========
        val textView = TextView(this).apply {
            textSize = 32f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#333333")) // 深灰色字体
            text = "你好，世界！"
            // 加粗字体
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            // 设置底边距，让文字和图片之间有距离
            setPadding(0, 0, 0, 60)
        }

        // ========== 3. 图片（前景图）优化 ==========
        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.chinese_flag)

            val params = LinearLayout.LayoutParams(600, 400)
            // 设置图片周围的外边距
            params.setMargins(0, 0, 0, 80)
            layoutParams = params

            // 进阶：使用代码设置圆角背景
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadius = 30f // 圆角大小
            shape.setColor(Color.WHITE) // 给图片加个白色底板

            scaleType = ImageView.ScaleType.CENTER_CROP // 让图片铺满整个容器，不会有留白
        }

        // ========== 4. 按钮（Button）美化 ==========
        val button = Button(this).apply {
            text = "切换语言 / SWITCH"
            textSize = 18f
            setTextColor(Color.WHITE) // 按钮文字变为白色

            // 使用 GradientDrawable 绘制一个圆角、蓝色的按钮背景
            val buttonShape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 50f // 按钮的圆角大小
                setColor(Color.parseColor("#2196F3")) // 按钮背景色：蓝色
                setStroke(2, Color.parseColor("#1976D2")) // 按钮边框：深蓝色
            }
            background = buttonShape

            // 设置按钮的内边距，让按钮看起来更充实
            setPadding(60, 30, 60, 30)
        }

        layout.addView(textView)
        layout.addView(imageView)
        layout.addView(button)

        setContentView(layout)

        // ========== 5. 点击事件（逻辑保持不变） ==========
        button.setOnClickListener {
            currentLangIndex = (currentLangIndex + 1) % 3

            when (currentLangIndex) {
                0 -> {
                    textView.text = "你好，世界！"
                    imageView.setImageResource(R.drawable.chinese_flag)
                }
                1 -> {
                    textView.text = "Hello World! "
                    imageView.setImageResource(R.drawable.american_flag)
                }
                2 -> {
                    textView.text = "Hello World! ！！"
                    imageView.setImageResource(R.drawable.english_flag)
                }
            }
        }
    }
}