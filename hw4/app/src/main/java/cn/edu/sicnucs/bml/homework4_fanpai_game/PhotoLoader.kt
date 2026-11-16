package cn.edu.sicnucs.bml.homework4_fanpai_game
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
object PhotoLoader {
 fun load(resources: Resources, id: Int): Bitmap {
  val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true }
  BitmapFactory.decodeResource(resources,id,bounds)
  var sample=1
  while(maxOf(bounds.outWidth,bounds.outHeight)/sample > 768) sample*=2
  return requireNotNull(BitmapFactory.decodeResource(resources,id,BitmapFactory.Options().apply { inSampleSize=sample; inScaled=false }))
 }
}
