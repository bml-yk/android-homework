package cn.edu.sicnucs.bml.homework4_mul_activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Activity3 : AppCompatActivity() {

    private lateinit var ivPhoto: ImageView

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photo = getCameraThumbnail(result.data)
            if (photo != null) {
                ivPhoto.setImageBitmap(photo)
            } else {
                showToast(R.string.camera_no_image)
            }
        } else {
            showToast(R.string.camera_cancelled)
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            showToast(R.string.camera_permission_denied)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3)
        title = getString(R.string.act3_title)

        ivPhoto = findViewById(R.id.iv_photo)

        findViewById<Button>(R.id.btn_take_photo).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(takePictureIntent)
        } else {
            showToast(R.string.camera_unavailable)
        }
    }

    @Suppress("DEPRECATION")
    private fun getCameraThumbnail(data: Intent?): Bitmap? {
        val key = getString(R.string.camera_result_data_key)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data?.extras?.getParcelable(key, Bitmap::class.java)
        } else {
            data?.extras?.getParcelable(key) as? Bitmap
        }
    }

    private fun showToast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }
}
