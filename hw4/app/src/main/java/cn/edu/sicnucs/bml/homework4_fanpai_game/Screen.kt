package cn.edu.sicnucs.bml.homework4_fanpai_game

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
abstract class Screen : AppCompatActivity() {
 fun showLayout(layout: Int) {
  enableEdgeToEdge(); setContentView(layout)
  ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
   val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
   v.setPadding(bars.left,bars.top,bars.right,bars.bottom); insets
  }
 }
}
