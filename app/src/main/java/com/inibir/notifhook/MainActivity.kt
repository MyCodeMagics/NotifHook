package com.inibir.notifhook

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

	private lateinit var pendingLabel: TextView
	private lateinit var adapter: ArrayAdapter<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<String>())

		val list = ListView(this).apply {
			addHeaderView(buildHeader(), null, false)
			adapter = this@MainActivity.adapter
		}

		setContentView(list)
	}

	override fun onResume() {
		super.onResume()
		refreshPending()
	}

	private fun buildHeader(): LinearLayout {
		val config = HookConfig(this)

		val urlInput = EditText(this).apply {
			hint = "https://tu-servidor/hook"
			inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
			setText(config.url)
		}

		val packagesInput = EditText(this).apply {
			hint = "Un paquete por linea (vacio = todas)\ncom.whatsapp"
			inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
			minLines = 4
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

		val versionLabel = TextView(this).apply {
			text = "Version ${packageManager.getPackageInfo(packageName, 0).versionName}"
		}

		val testButton = Button(this).apply {
			text = "Enviar notificacion de prueba"
			setOnClickListener { sendTest() }
		}

		pendingLabel = TextView(this).apply {
			textSize = 18f
			setPadding(0, 32, 0, 0)
		}

		val resendButton = Button(this).apply {
			text = "Reenviar pendientes"
			setOnClickListener { resend() }
		}

		return LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 24)
			addView(versionLabel)
			addView(urlInput)
			addView(packagesInput)
			addView(saveButton)
			addView(accessButton)
			addView(testButton)
			addView(pendingLabel)
			addView(resendButton)
		}
	}

	private fun resend() {
		Outbox.flushAsync(applicationContext) { complete ->
			runOnUiThread {
				val message = if (complete) "Enviado" else "No se pudo enviar"
				Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
			}
			refreshPending()
		}
	}

	private fun sendTest() {
		val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
			checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
		if (needsPermission) {
			requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
		} else {
			TestNotifier.send(this)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
		if (requestCode == REQUEST_NOTIFICATIONS && granted) TestNotifier.send(this)
	}

	private fun refreshPending() {
		Outbox.loadPending(applicationContext, PENDING_LIMIT) { total, rows ->
			runOnUiThread {
				pendingLabel.text = "Pendientes: $total"
				adapter.clear()
				adapter.addAll(rows.map(PendingFormatter::format))
			}
		}
	}

	private companion object {
		const val PENDING_LIMIT = 200
		const val REQUEST_NOTIFICATIONS = 1
	}
}
