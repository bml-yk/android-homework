package cn.edu.sicnucs.bml.homework6_fragments
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
object PhotoLoader {
 fun load(resources: Resources, id: Int): Bitmap {
  val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true }
  BitmapFactory.decodeResource(resources,id,bounds)
  var sample=1
  while(maxOf(bounds.outWidth,bounds.outHeight)/sample > 768) sample*=2
  return requireNotNull(BitmapFactory.decodeResource(resources,id,BitmapFactory.Options().apply { inSampleSize=sample; inScaled=false }))
 }

 fun load(file: File): Bitmap? {
  if (!file.exists()) return null
  val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  BitmapFactory.decodeFile(file.absolutePath, bounds)
  var sample = 1
  while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 768) sample *= 2
  return BitmapFactory.decodeFile(
      file.absolutePath,
      BitmapFactory.Options().apply {
        inSampleSize = sample
        inScaled = false
      }
  )
 }
}
