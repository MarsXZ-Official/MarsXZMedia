package com.marsxz.marsxzmedia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

class AboutActivity : AppCompatActivity() {

    companion object {
        private const val SUPPORT_EMAIL = "marsxz8656@gmail.com"
    }

    private lateinit var backButton: ImageButton
    private lateinit var tvSupportEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // ПРИМЕНЯЕМ ТЕМУ СО ШРИФТОМ
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        if (prefs.getString("font_type", "system") == "monocraft") {
            setTheme(R.style.Theme_MarsXZMedia_Monocraft)
        } else {
            setTheme(R.style.Theme_MarsXZMedia)
        }

        super.onCreate(savedInstanceState)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_about)
        UiSoundPlayer.init(this)

        backButton = findViewById(R.id.backButton)
        tvSupportEmail = findViewById(R.id.tvSupportEmail)

        tvSupportEmail.paintFlags = tvSupportEmail.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG

        backButton.setOnClickListener {
            UiSoundPlayer.playClick(this)
            finish()
        }

        tvSupportEmail.text = SUPPORT_EMAIL
        tvSupportEmail.setOnClickListener {
            UiSoundPlayer.playClick(this)
            sendEmail()
        }
    }

    private fun sendEmail() {
        val mailtoIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$SUPPORT_EMAIL")
            putExtra(Intent.EXTRA_SUBJECT, "MarsXZ Media Feedback (Android)")
        }

        try {
            startActivity(Intent.createChooser(mailtoIntent, "Отправить через..."))
        } catch (e: Exception) {
            val gmailBrowserUri = Uri.parse(
                "https://mail.google.com/mail/?view=cm&fs=1&to=${Uri.encode(SUPPORT_EMAIL)}&su=${Uri.encode("MarsXZ Media Feedback")}"
            )
            val browserIntent = Intent(Intent.ACTION_VIEW, gmailBrowserUri)

            try {
                startActivity(browserIntent)
                Toast.makeText(this, "Открываю Gmail в браузере...", Toast.LENGTH_SHORT).show()
            } catch (e2: Exception) {
                copyToClipboard(SUPPORT_EMAIL)
                Toast.makeText(this, "Почта скопирована в буфер обмена", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("MarsXZ Support", text)
        clipboard.setPrimaryClip(clip)
    }
}
