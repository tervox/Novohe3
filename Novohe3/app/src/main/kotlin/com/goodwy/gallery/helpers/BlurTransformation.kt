package com.goodwy.gallery.helpers

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class BlurTransformation(
    private val context: Context,
    private val radius: Int = 25
) : BitmapTransformation() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val output = pool.get(toTransform.width, toTransform.height, Bitmap.Config.ARGB_8888)
        output.density = toTransform.density

        try {
            val rs = RenderScript.create(context)
            val input = Allocation.createFromBitmap(rs, toTransform,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
            val out = Allocation.createTyped(rs, input.type)
            val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            script.setRadius(radius.toFloat().coerceIn(1f, 25f))
            script.setInput(input)
            script.forEach(out)
            out.copyTo(output)
            input.destroy()
            out.destroy()
            script.destroy()
            rs.destroy()
        } catch (e: Exception) {
            return toTransform
        }

        return output
    }

    override fun equals(other: Any?) = other is BlurTransformation && other.radius == radius
    override fun hashCode() = radius
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("BlurTransformation$radius".toByteArray())
    }
}
