package cn.edu.sicnucs.bml.homework5_recyclerview

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
abstract class Screen : AppCompatActivity() {
 fun showLayout(layout: Int) {
  enableEdgeToEdge(); setContentView(layout)
  val root = findViewById<android.view.View>(R.id.main)
  val initialLeft = root.paddingLeft
  val initialTop = root.paddingTop
  val initialRight = root.paddingRight
  val initialBottom = root.paddingBottom
  ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
   val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
   v.setPadding(
    initialLeft + bars.left,
    initialTop + bars.top,
    initialRight + bars.right,
    initialBottom + bars.bottom
   )
   insets
  }
 }
}
