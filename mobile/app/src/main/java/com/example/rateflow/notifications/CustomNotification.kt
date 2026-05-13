package com.example.rateflow.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.rateflow.R

enum class NotificationType {
    SUCCESS, ERROR, INFO, WARNING
}

class CustomNotification(private val context: Context) {

    private var notificationView: View? = null
    private var containerLayout: FrameLayout? = null
    private var autoHideHandler = Handler(Looper.getMainLooper())
    private var autoHideRunnable: Runnable? = null
    private var isShowing = false

    fun show(
        message: String,
        title: String = "",
        type: NotificationType = NotificationType.INFO,
        duration: Long = 3000,
        autoHide: Boolean = true
    ) {
        // Dismiss any existing notification
        dismiss()

        // Find the root view
        val rootView = (context as? androidx.appcompat.app.AppCompatActivity)?.findViewById<ViewGroup>(android.R.id.content)
            ?: return

        // Create container frame if not exists
        if (containerLayout == null) {
            containerLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            rootView.addView(containerLayout)
        }

        // Inflate notification view
        notificationView = LayoutInflater.from(context).inflate(R.layout.custom_dropdown_notification, containerLayout, false)

        // Setup notification content
        val ivIcon = notificationView?.findViewById<ImageView>(R.id.ivNotificationIcon)
        val tvTitle = notificationView?.findViewById<TextView>(R.id.tvNotificationTitle)
        val tvMessage = notificationView?.findViewById<TextView>(R.id.tvNotificationMessage)
        val ivClose = notificationView?.findViewById<ImageView>(R.id.ivCloseNotification)

        tvMessage?.text = message

        // Set title
        if (title.isNotEmpty()) {
            tvTitle?.text = title
            tvTitle?.visibility = View.VISIBLE
        } else {
            tvTitle?.visibility = View.GONE
        }

        // Set icon and background color based on type
        when (type) {
            NotificationType.SUCCESS -> {
                ivIcon?.setImageResource(R.drawable.ic_check_circle)
                notificationView?.setBackgroundResource(R.drawable.background_with_border)
            }
            NotificationType.ERROR -> {
                ivIcon?.setImageResource(R.drawable.ic_warning)
                notificationView?.setBackgroundResource(R.drawable.bg_notification_error)
            }
            NotificationType.WARNING -> {
                ivIcon?.setImageResource(R.drawable.ic_warning)
                notificationView?.setBackgroundResource(R.drawable.bg_notification_warning)
            }
            NotificationType.INFO -> {
                ivIcon?.setImageResource(R.drawable.ic_info)
                notificationView?.setBackgroundResource(R.drawable.background_with_border)
            }
        }

        // Setup close button
        ivClose?.setOnClickListener {
            dismiss()
        }

        // Add view to container
        containerLayout?.addView(notificationView)

        // Apply slide-in animation
        val slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_down)
        notificationView?.startAnimation(slideIn)
        notificationView?.visibility = View.VISIBLE
        isShowing = true

        // Auto-hide after duration
        if (autoHide) {
            autoHideRunnable = Runnable {
                if (isShowing) {
                    dismiss()
                }
            }
            autoHideHandler.postDelayed(autoHideRunnable!!, duration)
        }
    }

    fun dismiss() {
        if (!isShowing || notificationView == null) return

        autoHideRunnable?.let { autoHideHandler.removeCallbacks(it) }

        val slideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out_up)
        slideOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}

            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                notificationView?.visibility = View.GONE
                containerLayout?.removeView(notificationView)
                notificationView = null
                isShowing = false
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })

        notificationView?.startAnimation(slideOut)
    }

    fun isShowing(): Boolean = isShowing
}