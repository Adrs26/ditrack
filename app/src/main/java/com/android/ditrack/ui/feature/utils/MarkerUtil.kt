package com.android.ditrack.ui.feature.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MarkerUtil {
    fun createBusStopMarker(context: Context, iconRes: Int): BitmapDescriptor {
        val totalSize = 80
        val outerCircleOffset = 2f
        val outerCircleRadius = 100f
        val innerCircleSize = 60f
        val innerCircleOffset = (totalSize - innerCircleSize) / 2f
        val innerCircleRadius = outerCircleRadius * 0.6f
        val pointerRadius = 8f
        val totalHeight = totalSize + pointerRadius.toInt()
        val bitmap = createBitmap(totalSize, totalHeight)
        val canvas = Canvas(bitmap)

        val whiteBackground = Paint().apply {
            color = "#F8FCFB".toColorInt()
            isAntiAlias = true
        }

        val redBackground = Paint().apply {
            color = "#EF5350".toColorInt()
            isAntiAlias = true
        }

        val outerCircleRect = RectF(
            outerCircleOffset,
            outerCircleOffset,
            totalSize - outerCircleOffset,
            totalSize - outerCircleOffset
        )
        val innerCircleRect = RectF(
            innerCircleOffset,
            innerCircleOffset,
            innerCircleOffset + innerCircleSize,
            innerCircleOffset + innerCircleSize
        )

        canvas.drawRoundRect(
            outerCircleRect,
            outerCircleRadius,
            outerCircleRadius,
            redBackground
        )
        canvas.drawRoundRect(
            innerCircleRect,
            innerCircleRadius,
            innerCircleRadius,
            whiteBackground
        )

        val pointerCenterX = totalSize / 2f
        val pointerCenterY = totalSize - 2f

        canvas.drawCircle(
            pointerCenterX,
            pointerCenterY,
            pointerRadius,
            redBackground
        )

        val iconDrawable = ContextCompat.getDrawable(context, iconRes)

        iconDrawable?.let { icon ->
            icon.setTint("#EF5350".toColorInt())

            val iconSize = 48
            val centerX = totalSize / 2
            val centerY = totalSize / 2

            val iconLeft = centerX - iconSize / 2
            val iconTop = centerY - iconSize / 2
            val iconRight = iconLeft + iconSize
            val iconBottom = iconTop + iconSize

            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            icon.draw(canvas)
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun createBusMarker(context: Context, iconRes: Int): BitmapDescriptor {
        val totalSize = 70
        val outerRectOffset = 2f
        val outerCornerRadius = 24f
        val innerRectSize = 50f
        val innerRectOffset = (totalSize - innerRectSize) / 2f
        val innerCornerRadius = 16f
        val bitmap = createBitmap(totalSize, totalSize)
        val canvas = Canvas(bitmap)

        val whiteBackground = Paint().apply {
            color = "#F8FCFB".toColorInt()
            isAntiAlias = true
        }

        val grayBackground = Paint().apply {
            color = "#36454F".toColorInt()
            isAntiAlias = true
        }

        val outerRectF = RectF(
            outerRectOffset,
            outerRectOffset,
            totalSize - outerRectOffset,
            totalSize - outerRectOffset
        )
        canvas.drawRoundRect(
            outerRectF,
            outerCornerRadius,
            outerCornerRadius,
            grayBackground
        )

        val innerRectF = RectF(
            innerRectOffset,
            innerRectOffset,
            innerRectOffset + innerRectSize,
            innerRectOffset + innerRectSize
        )
        canvas.drawRoundRect(
            innerRectF,
            innerCornerRadius,
            innerCornerRadius,
            whiteBackground
        )

        val iconDrawable = ContextCompat.getDrawable(context, iconRes)

        iconDrawable?.let { icon ->
            icon.setTint("#36454F".toColorInt())

            val iconSize = 44
            val centerX = totalSize / 2
            val centerY = totalSize / 2

            val iconLeft = centerX - iconSize / 2
            val iconTop = centerY - iconSize / 2
            val iconRight = iconLeft + iconSize
            val iconBottom = iconTop + iconSize

            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            icon.draw(canvas)
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}