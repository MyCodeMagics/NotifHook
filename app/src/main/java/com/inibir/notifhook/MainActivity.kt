package com.inibir.notifhook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class MainActivity : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val config = HookConfig(this)

		val urlInput = EditText(this).apply {
			hint = "https://tu-servidor/hook"
			inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
			setText(config.url)
		}

		val packagesInput = EditText(this).apply {
			hint = "Un paquete por linea (vacio = todas)\ncom.whatsapp"
			inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
			minLines = 6
			gravity = Gravity.TOP
			setText(config.packagesText)
		}

		val saveButton = Button(this).apply {
			text = "Guardar"
			setOnClickListener {
				config.url = urlInput.text.toString()
				config.packagesText = packagesInput.text.toString()
				Toast.makeText(this@MainActivity, "Guardado", Toast.LENGTH_SHORT).show()
			}
		}

		val accessButton = Button(this).apply {
			text = "Acceso a notificaciones"
			setOnClickListener {
				startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
			}
		}

		val root = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 48)
			addView(urlInput)
			addView(packagesInput)
			addView(saveButton)
			addView(accessButton)
		}

		setContentView(root)
	}
}
