package com.inibir.notifhook

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

	private lateinit var pendingLabel: TextView
	private lateinit var serviceStatusLabel: TextView
	private lateinit var batteryStatusLabel: TextView
	private lateinit var batteryButton: Button
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
		if (ServiceRebinder.isAccessGranted(this) && !NotifListener.isConnected) {
			ServiceRebinder.rebind(this)
		}
		updateStatus()
		refreshPending()
	}

	private fun buildHeader(): LinearLayout {
		val config = HookConfig(this)

		val urlInput = EditText(this).apply {
			hint = "https://tu-servidor/hook"
			inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
			setText(config.url)
		}

		val saveButton = Button(this).apply {
			text = "Guardar"
			setOnClickListener {
				config.url = urlInput.text.toString()
				Toast.makeText(this@MainActivity, "Guardado", Toast.LENGTH_SHORT).show()
			}
		}

		val appsButton = Button(this).apply {
			text = "Elegir aplicaciones"
			setOnClickListener { startActivity(Intent(this@MainActivity, AppsActivity::class.java)) }
		}

		val accessButton = Button(this).apply {
			text = "Acceso a notificaciones"
			setOnClickListener {
				startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
			}
		}

		val rebindButton = Button(this).apply {
			text = "Reconectar servicio"
			setOnClickListener {
				ServiceRebinder.rebind(this@MainActivity)
				Toast.makeText(this@MainActivity, "Revinculando servicio...", Toast.LENGTH_SHORT).show()
				updateStatus()
			}
		}

		batteryButton = Button(this).apply {
			text = "Desactivar ahorro de bateria"
			setOnClickListener {
				BatteryOptimizer.requestIgnore(this@MainActivity)
			}
		}

		val versionLabel = TextView(this).apply {
			text = "Version ${packageManager.getPackageInfo(packageName, 0).versionName}"
		}

		serviceStatusLabel = TextView(this).apply {
			setPadding(0, 16, 0, 8)
		}

		batteryStatusLabel = TextView(this).apply {
			setPadding(0, 0, 0, 16)
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
			addView(serviceStatusLabel)
			addView(batteryStatusLabel)
			addView(urlInput)
			addView(saveButton)
			addView(appsButton)
			addView(accessButton)
			addView(rebindButton)
			addView(batteryButton)
			addView(testButton)
			addView(pendingLabel)
			addView(resendButton)
		}
	}

	private fun updateStatus() {
		val hasAccess = ServiceRebinder.isAccessGranted(this)
		val isConnected = NotifListener.isConnected
		serviceStatusLabel.text = when {
			!hasAccess -> "Estado: Sin permiso de notificaciones"
			isConnected -> "Estado: Servicio conectado y activo"
			else -> "Estado: Servicio desconectado (Pulsa Reconectar)"
		}

		val ignoringBattery = BatteryOptimizer.isIgnoring(this)
		batteryStatusLabel.text = if (ignoringBattery) {
			"Bateria: Sin restricciones (No se apagara)"
		} else {
			"Bateria: Optimizada (Android puede apagar el servicio)"
		}
		batteryButton.isEnabled = !ignoringBattery
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
