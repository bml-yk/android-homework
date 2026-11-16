package cn.edu.sicnucs.bml.homework6_fragments

import android.content.Context
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class HistoryStore(context: Context) {
    private val appContext = context.applicationContext

    fun load(): AppState {
        return try {
            appContext.openFileInput(FILE_NAME).use { fileInput ->
                ObjectInputStream(fileInput).use { objectInput ->
                    objectInput.readObject() as? AppState ?: AppState()
                }
            }
        } catch (_: Exception) {
            AppState()
        }
    }

    fun save(state: AppState) {
        try {
            appContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { fileOutput ->
                ObjectOutputStream(fileOutput).use { objectOutput ->
                    objectOutput.writeObject(state)
                }
            }
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    companion object {
        const val FILE_NAME = "homework6_state.ser"
    }
}
