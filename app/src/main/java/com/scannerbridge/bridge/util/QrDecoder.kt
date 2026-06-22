package com.scannerbridge.bridge.util

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

/**
 * Decodes a QR code directly from a UVC NV21 frame. ZXing only needs the
 * luminance (Y) plane, which is the first width*height bytes of NV21, so we
 * can feed the raw frame straight in with zero conversion.
 */
class QrDecoder {

    private val reader = QRCodeReader()
    private val hints = mapOf(DecodeHintType.TRY_HARDER to true)

    /**
     * @return decoded text, or null if no QR found in this frame.
     */
    fun decodeNv21(nv21: ByteArray, width: Int, height: Int): String? {
        if (width <= 0 || height <= 0) return null
        return tryDecode(nv21, width, height, false)
            ?: tryDecode(nv21, width, height, true)   // retry inverted
    }

    private fun tryDecode(
        nv21: ByteArray, width: Int, height: Int, invert: Boolean
    ): String? {
        return try {
            val source = PlanarYUVLuminanceSource(
                nv21, width, height, 0, 0, width, height, false
            )
            val lumi = if (invert) source.invert() else source
            val bitmap = BinaryBitmap(HybridBinarizer(lumi))
            reader.decode(bitmap, hints).text
        } catch (_: Throwable) {
            null
        } finally {
            reader.reset()
        }
    }
}
