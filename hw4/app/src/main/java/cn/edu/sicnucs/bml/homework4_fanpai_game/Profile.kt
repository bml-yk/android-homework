package cn.edu.sicnucs.bml.homework4_fanpai_game

import android.os.Bundle
import android.widget.ImageView
object Profile {
 const val KEY = "profile"
 val fields = listOf(Triple("name",R.id.et_name,R.string.name),Triple("student",R.id.et_student,R.string.student),Triple("phone",R.id.et_phone,R.string.phone),Triple("email",R.id.et_email,R.string.email))
 val labels = listOf(R.id.tv_name,R.id.tv_student,R.id.tv_phone,R.id.tv_email)
 val hobbies = listOf(Triple("travel",R.id.travel,R.string.travel),Triple("reading",R.id.reading,R.string.reading),Triple("gaming",R.id.gaming,R.string.gaming),Triple("shopping",R.id.shopping,R.string.shopping))
 fun avatar(view: ImageView, data: Bundle) {
  view.setImageBitmap(PhotoLoader.load(view.resources,R.drawable.photo_2))
  data.getString("avatar")?.let {
   try { view.setImageURI(android.net.Uri.parse(it)) } catch (_: SecurityException) { view.setImageBitmap(PhotoLoader.load(view.resources,R.drawable.photo_2)) }
  }
 }
}
