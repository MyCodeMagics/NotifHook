package com.inibir.notifhook

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

	private lateinit var pendingLabel: TextView
	private lateinit var serviceStatusLabel: TextView
	private lateinit var batteryStatusLabel: TextView
	private lateinit var batteryButton: ImageButton
	private lateinit var adapter: ArrayAdapter<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		startForegroundService(Intent(this, KeepAliveForegroundService::class.java))
		ServiceRebinder.rebind(this)

		if (!BatteryOptimizer.isIgnoring(this)) {
			BatteryOptimizer.requestIgnore(this)
		}

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

		val versionLabel = TextView(this).apply {
			text = "Version ${packageManager.getPackageInfo(packageName, 0).versionName}"
		}

		val infoButton = ImageButton(this).apply {
			setImageResource(R.drawable.ic_info)
			contentDescription = "Guia de segundo plano"
			setPadding(16, 16, 16, 16)
			setOnClickListener {
				KeepAliveGuideDialog.show(this@MainActivity) {
					updateStatus()
				}
			}
		}

		val topRow = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
			gravity = Gravity.CENTER_VERTICAL
			addView(versionLabel, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
			addView(infoButton, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
		}

		serviceStatusLabel = TextView(this).apply {
			setPadding(0, 16, 0, 8)
		}

		batteryStatusLabel = TextView(this).apply {
			setPadding(0, 0, 0, 16)
		}

		val urlInput = EditText(this).apply {
			hint = "https://tu-servidor/hook"
			inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
			setText(config.url)
		}

		val saveButton = ImageButton(this).apply {
			setImageResource(R.drawable.ic_save)
			contentDescription = "Guardar"
			setPadding(24, 24, 24, 24)
			setOnClickListener {
				config.url = urlInput.text.toString()
				Toast.makeText(this@MainActivity, "Guardado", Toast.LENGTH_SHORT).show()
			}
		}

		val urlRow = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
			gravity = Gravity.CENTER_VERTICAL
			addView(urlInput, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
			val buttonParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			).apply {
				setMargins(16, 0, 0, 0)
			}
			addView(saveButton, buttonParams)
		}

		val appsButton = ImageButton(this).apply {
			setImageResource(R.drawable.ic_apps)
			contentDescription = "Elegir aplicaciones"
			setPadding(24, 24, 24, 24)
			setOnClickListener { startActivity(Intent(this@MainActivity, AppsActivity::class.java)) }
		}

		val accessButton = ImageButton(this).apply {
			setImageResource(R.drawable.ic_settings)
			contentDescription = "Acceso a notificaciones"
			setPadding(24, 24, 24, 24)
			setOnClickListener {
				startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
			}
		}

		batteryButton = ImageButton(this).apply {
			setImageResource(R.drawable.ic_battery)
			contentDescription = "Ahorro de bateria"
			setPadding(24, 24, 24, 24)
			setOnClickListener {
				if (BatteryOptimizer.isIgnoring(this@MainActivity)) {
					Toast.makeText(this@MainActivity, "Bateria ya sin restricciones", Toast.LENGTH_SHORT).show()
				} else {
					BatteryOptimizer.requestIgnore(this@MainActivity)
				}
			}
		}

		val rebindButton = ImageButton(this).apply {
			setImageResource(R.drawable.ic_refresh)
			contentDescription = "Reiniciar servicio"
			setPadding(24, 24, 24, 24)
			setOnClickListener {
				ServiceRebinder.rebind(this@MainActivity)
				Toast.makeText(this@MainActivity, "Revinculando servicio...", Toast.LENGTH_SHORT).show()
				updateStatus()
			}
		}

		val configRow = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
			val itemParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
				setMargins(8, 16, 8, 16)
			}
			addView(appsButton, itemParams)
			addView(accessButton, LinearLayout.LayoutParams(itemParams))
			addView(batteryButton, LinearLayout.LayoutParams(itemParams))
			addView(rebindButton, LinearLayout.LayoutParams(itemParams))
		}

		val testButton = Button(this).apply {
			text = "test"
			setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_send, 0, 0, 0)
			compoundDrawablePadding = 16
			setOnClickListener { sendTest() }
		}

		val resendButton = Button(this).apply {
			text = "push"
			setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upload, 0, 0, 0)
			compoundDrawablePadding = 16
			setOnClickListener { resend() }
		}

		val actionsRow = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
			val actionParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
				setMargins(8, 0, 8, 0)
			}
			addView(testButton, actionParams)
			addView(resendButton, LinearLayout.LayoutParams(actionParams))
		}

		pendingLabel = TextView(this).apply {
			textSize = 16f
			setPadding(8, 24, 8, 8)
		}

		return LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 24)
			addView(topRow)
			addView(serviceStatusLabel)
			addView(batteryStatusLabel)
			addView(urlRow)
			addView(configRow)
			addView(pendingLabel)
			addView(actionsRow)
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
		batteryButton.alpha = if (ignoringBattery) 1.0f else 0.5f
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
