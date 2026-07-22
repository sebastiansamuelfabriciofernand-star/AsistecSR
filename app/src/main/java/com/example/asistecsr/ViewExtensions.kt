package com.example.asistecsr

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

/**
 * Extensión para aplicar una animación de escala (presión) a cualquier vista.
 * Se encoge un poco al tocar y vuelve a su tamaño al soltar, ejecutando la acción.
 */
@SuppressLint("ClickableViewAccessibility")
fun View.setupClickAnimation(action: () -> Unit) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
            }
            MotionEvent.ACTION_UP -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                v.performClick()
                action()
            }
            MotionEvent.ACTION_CANCEL -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
        }
        true
    }
}

/**
 * Especial para la selección de roles: Se ilumina con un color antes de navegar.
 */
@SuppressLint("ClickableViewAccessibility")
fun View.setupRoleSelectionAnimation(highlightColorRes: Int, action: () -> Unit) {
    val originalColor = Color.parseColor("#232233") 
    
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start()
                // Iluminación
                v.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(v.context, highlightColorRes))
            }
            MotionEvent.ACTION_UP -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                v.performClick()
                
                // Pequeño retraso para que se vea la iluminación antes de cambiar de pantalla
                Handler(Looper.getMainLooper()).postDelayed({
                    v.backgroundTintList = ColorStateList.valueOf(originalColor)
                    action()
                }, 180)
            }
            MotionEvent.ACTION_CANCEL -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                v.backgroundTintList = ColorStateList.valueOf(originalColor)
            }
        }
        true
    }
}