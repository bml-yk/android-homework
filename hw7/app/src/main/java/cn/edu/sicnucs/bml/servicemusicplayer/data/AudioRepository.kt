package cn.edu.sicnucs.bml.servicemusicplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore

class AudioRepository(private val contentResolver: ContentResolver) {

    fun loadAudioTracks(): List<AudioTrack> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
            "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf("1000")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        return buildList {
            contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder,
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn).orEmpty()
                    val title = cursor.getString(titleColumn)
                        ?.takeIf { it.isNotBlank() }
                        ?: displayName.substringBeforeLast('.')
                    val artist = cursor.getString(artistColumn)
                        ?.takeUnless { it == MediaStore.UNKNOWN_STRING }
                        ?: "未知艺术家"

                    add(
                        AudioTrack(
                            id = id,
                            title = title,
                            artist = artist,
                            duration = cursor.getLong(durationColumn),
                            uri = ContentUris.withAppendedId(collection, id),
                        ),
                    )
                }
            }
        }
    }
}
