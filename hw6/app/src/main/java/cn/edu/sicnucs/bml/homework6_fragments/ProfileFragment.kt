package cn.edu.sicnucs.bml.homework6_fragments

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.File

class ProfileFragment : Fragment() {
    private var avatarFile: String? = null

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyAvatarToInternalStorage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = requireActivity() as AppStateHost
        val profile = host.appState.profile
        avatarFile = profile.avatarFile

        view.findViewById<EditText>(R.id.et_name).setText(profile.name)
        view.findViewById<EditText>(R.id.et_student).setText(profile.student)
        view.findViewById<EditText>(R.id.et_phone).setText(profile.phone)
        view.findViewById<EditText>(R.id.et_email).setText(profile.email)

        when (profile.gender) {
            1 -> view.findViewById<RadioGroup>(R.id.genderGroup).check(R.id.male)
            2 -> view.findViewById<RadioGroup>(R.id.genderGroup).check(R.id.female)
        }
        hobbyViews.forEach { (key, id) ->
            view.findViewById<CheckBox>(id).isChecked = key in profile.hobbies
        }

        showAvatar(view.findViewById(R.id.avatar))
        updateBestScores(view.findViewById(R.id.tvProfileBest))
        view.findViewById<Button>(R.id.btnAvatar).setOnClickListener {
            pickAvatar.launch("image/*")
        }
        view.findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            saveProfile(view)
        }
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<TextView>(R.id.tvProfileBest)?.let(::updateBestScores)
    }

    private fun saveProfile(view: View) {
        val name = view.findViewById<EditText>(R.id.et_name)
        val email = view.findViewById<EditText>(R.id.et_email)
        if (name.text.toString().isBlank()) {
            name.error = getString(R.string.required)
            name.requestFocus()
            return
        }
        val emailText = email.text.toString().trim()
        if (emailText.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.error = getString(R.string.invalid_email)
            email.requestFocus()
            return
        }

        val host = requireActivity() as AppStateHost
        val gender = when (view.findViewById<RadioGroup>(R.id.genderGroup).checkedRadioButtonId) {
            R.id.male -> 1
            R.id.female -> 2
            else -> 0
        }
        val hobbies = hobbyViews
            .filter { (_, id) -> view.findViewById<CheckBox>(id).isChecked }
            .map { it.first }
        host.appState.profile = host.appState.profile.copyFrom(
            name = name.text.toString().trim(),
            student = view.findViewById<EditText>(R.id.et_student).text.toString().trim(),
            phone = view.findViewById<EditText>(R.id.et_phone).text.toString().trim(),
            email = emailText,
            gender = gender,
            hobbies = hobbies
        )
        host.persistState()
        Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show()
    }

    private fun copyAvatarToInternalStorage(uri: Uri) {
        val context = requireContext()
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                context.openFileOutput(AVATAR_FILE, Context.MODE_PRIVATE).use { output ->
                    input.copyTo(output)
                }
            } ?: return
            avatarFile = AVATAR_FILE
            val host = requireActivity() as AppStateHost
            host.appState.profile.avatarFile = AVATAR_FILE
            host.persistState()
            showAvatar(requireView().findViewById(R.id.avatar))
        } catch (_: Exception) {
            Toast.makeText(context, R.string.avatar_load_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAvatar(view: ImageView) {
        val bitmap: Bitmap? = avatarFile?.let {
            PhotoLoader.load(File(requireContext().filesDir, it))
        }
        if (bitmap != null) {
            view.setImageBitmap(bitmap)
        } else {
            view.setImageBitmap(PhotoLoader.load(resources, R.drawable.photo_2))
        }
    }

    private fun updateBestScores(view: TextView) {
        val state = (requireActivity() as AppStateHost).appState
        view.text = getString(
            R.string.profile_best_scores,
            state.twoCardBest,
            state.threeCardBest
        )
    }

    companion object {
        const val TAG = "ProfileFragment"
        private const val AVATAR_FILE = "profile_avatar.jpg"
        private val hobbyViews = listOf(
            "travel" to R.id.travel,
            "reading" to R.id.reading,
            "gaming" to R.id.gaming,
            "shopping" to R.id.shopping
        )
    }
}
