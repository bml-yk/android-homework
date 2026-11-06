package cn.edu.sicnucs.bml.homework5_recyclerview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : Screen() {
 private var profile = Bundle()
 private var highScore = 0
 private val edit = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
  if(r.resultCode == Activity.RESULT_OK) { r.data?.getBundleExtra(Profile.KEY)?.let { profile = Bundle(it) }; render() }
 }
 private val game = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
  if(r.resultCode == Activity.RESULT_OK) { highScore = maxOf(highScore,r.data?.getIntExtra(GameActivity.BEST,0) ?: 0); render() }
 }
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState); showLayout(R.layout.activity_main)
  setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
  profile = savedInstanceState?.getBundle(Profile.KEY) ?: Bundle()
  highScore = savedInstanceState?.getInt(GameActivity.BEST) ?: 0
  val openEditor = {
   edit.launch(Intent(this,EditProfileActivity::class.java).putExtra(Profile.KEY,profile))
  }
  findViewById<Button>(R.id.btnEditProfile).setOnClickListener { openEditor() }
  (Profile.labels + listOf(R.id.tv_gender,R.id.tv_hobbies)).forEach { id ->
   findViewById<TextView>(id).setOnClickListener { openEditor() }
  }
  findViewById<ImageView>(R.id.avatar).setOnClickListener { openEditor() }
  findViewById<Button>(R.id.btnStartGame).setOnClickListener { game.launch(Intent(this,GameActivity::class.java)) }
  render()
 }

 override fun onCreateOptionsMenu(menu: Menu): Boolean {
  menuInflater.inflate(R.menu.main_menu, menu)
  return true
 }

 override fun onOptionsItemSelected(item: MenuItem): Boolean {
  if (item.itemId == R.id.action_about) {
   AlertDialog.Builder(this)
    .setTitle(R.string.about_title)
    .setMessage(R.string.about_message)
    .setPositiveButton(R.string.ok, null)
    .show()
   return true
  }
  return super.onOptionsItemSelected(item)
 }

 private fun render() {
  Profile.fields.forEachIndexed { i,(key,_,label) ->
   findViewById<TextView>(Profile.labels[i]).text = getString(R.string.field_value,getString(label),profile.getString(key).orEmpty().ifBlank { getString(R.string.unset) })
  }
  val gender = when(profile.getInt("gender")) { 1 -> R.string.male; 2 -> R.string.female; else -> R.string.unset }
  findViewById<TextView>(R.id.tv_gender).text = getString(R.string.field_value,getString(R.string.gender),getString(gender))
  val hobbies = Profile.hobbies.filter { profile.getBoolean(it.first) }.map { getString(it.third) }.joinToString(getString(R.string.separator)).ifBlank { getString(R.string.unset) }
  findViewById<TextView>(R.id.tv_hobbies).text = getString(R.string.field_value,getString(R.string.hobbies),hobbies)
  findViewById<TextView>(R.id.tv_best).text = getString(R.string.best_value,highScore)
  Profile.avatar(findViewById(R.id.avatar),profile)
 }
 override fun onSaveInstanceState(outState: Bundle) {
  outState.putBundle(Profile.KEY,profile); outState.putInt(GameActivity.BEST,highScore); super.onSaveInstanceState(outState)
 }
}
