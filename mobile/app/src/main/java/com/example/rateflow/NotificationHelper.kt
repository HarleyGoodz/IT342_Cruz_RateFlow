package com.example.rateflow

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat

object NotificationHelper {

    enum class Type { SUCCESS, ERROR, WARNING }

    fun show(activity: Activity, message: String, type: Type) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.notification_banner, null)

        val icon = view.findViewById<ImageView>(R.id.notifIcon)
        val text = view.findViewById<TextView>(R.id.notifMessage)
        val container = view.findViewById<View>(R.id.notifContainer)

        text.text = message

        when (type) {
            Type.SUCCESS -> {
                container.setBackgroundResource(R.drawable.notif_bg_success)
                icon.setImageResource(R.drawable.ic_check_circle)
                icon.setColorFilter(ContextCompat.getColor(activity, R.color.notif_success_icon))
            }
            Type.ERROR -> {
                container.setBackgroundResource(R.drawable.notif_bg_error)
                icon.setImageResource(R.drawable.ic_error_circle)
                icon.setColorFilter(ContextCompat.getColor(activity, R.color.notif_error_icon))
            }
            Type.WARNING -> {
                container.setBackgroundResource(R.drawable.notif_bg_warning)
                icon.setImageResource(R.drawable.ic_warning)
                icon.setColorFilter(ContextCompat.getColor(activity, R.color.notif_warning_icon))
            }
        }

        val popup = PopupWindow(
            view,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        popup.isOutsideTouchable = true
        popup.elevation = 24f

        val slideIn = AnimationUtils.loadAnimation(activity, R.anim.slide_in_top)
        view.startAnimation(slideIn)

        val rootView = activity.window.decorView.rootView
        popup.showAtLocation(rootView, Gravity.TOP, 0, 0)

        view.postDelayed({
            val slideOut = AnimationUtils.loadAnimation(activity, R.anim.slide_out_top)
            view.startAnimation(slideOut)
            view.postDelayed({ popup.dismiss() }, 300)
        }, 3000)
    }
}