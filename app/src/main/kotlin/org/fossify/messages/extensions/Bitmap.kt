package org.fossify.messages.extensions

import android.graphics.Bitmap

// the WEBP constant is deprecated in favour of WEBP_LOSSY and WEBP_LOSSLESS, all of them are saved as a webp file,
// so the formats are matched by name here to avoid the deprecated constant
fun Bitmap.CompressFormat.extension() = when {
    name == "PNG" -> "png"
    name.startsWith("WEBP") -> "webp"
    else -> "jpg"
}
