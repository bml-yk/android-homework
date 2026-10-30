package cn.edu.sicnucs.bml.homework4_fanpai_game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
class EditProfileActivity : Screen() {
 private var profile = Bundle()
 private val pickAvatar = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
  if(uri != null) { profile.putString("avatar",uri.toString()); Profile.avatar(findViewById(R.id.avatar),profile) }
 }
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState); showLayout(R.layout.activity_edit_profile)
  profile = Bundle(savedInstanceState?.getBundle(Profile.KEY) ?: intent.getBundleExtra(Profile.KEY) ?: Bundle())
  Profile.fields.forEach { (key,id,_) -> findViewById<EditText>(id).setText(profile.getString(key).orEmpty()) }
  when(profile.getInt("gender")) { 1 -> findViewById<RadioGroup>(R.id.genderGroup).check(R.id.male); 2 -> findViewById<RadioGroup>(R.id.genderGroup).check(R.id.female) }
  Profile.hobbies.forEach { (key,id,_) -> findViewById<CheckBox>(id).isChecked = profile.getBoolean(key) }
  Profile.avatar(findViewById(R.id.avatar),profile)
  findViewById<Button>(R.id.btnAvatar).setOnClickListener { pickAvatar.launch("image/*") }
  findViewById<Button>(R.id.btnCancel).setOnClickListener { setResult(Activity.RESULT_CANCELED); finish() }
  findViewById<Button>(R.id.btnSave).setOnClickListener {
   val name = findViewById<EditText>(R.id.et_name)
   if(name.text.toString().isBlank()) { name.error=getString(R.string.required); name.requestFocus(); return@setOnClickListener }
   val email = findViewById<EditText>(R.id.et_email)
   val address = email.text.toString().trim()
   if(address.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) { email.error=getString(R.string.invalid_email); return@setOnClickListener }
   collect(); setResult(Activity.RESULT_OK,Intent().putExtra(Profile.KEY,profile)); finish()
  }
 }
 private fun collect() {
  Profile.fields.forEach { (key,id,_) -> profile.putString(key,findViewById<EditText>(id).text.toString().trim()) }
  profile.putInt("gender",when(findViewById<RadioGroup>(R.id.genderGroup).checkedRadioButtonId) { R.id.male -> 1; R.id.female -> 2; else -> 0 })
  Profile.hobbies.forEach { (key,id,_) -> profile.putBoolean(key,findViewById<CheckBox>(id).isChecked) }
 }
 override fun onSaveInstanceState(outState: Bundle) { collect(); outState.putBundle(Profile.KEY,profile); super.onSaveInstanceState(outState) }
}
