package com.inibir.notifhook

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

object KeepAliveGuideDialog {

	private data class GuideStep(
		val id: String,
		val title: String,
		val promptMessage: String,
		val isDone: (Context) -> Boolean,
		val onActivate: (Context) -> Unit
	)

	fun show(activity: Activity, onDismiss: () -> Unit = {}) {
		val steps = listOf(
			GuideStep(
				id = "listener",
				title = "Acceso a notificaciones",
				promptMessage = "Activa el acceso a notificaciones para NotifHook en los ajustes del sistema.",
				isDone = { ctx -> ServiceRebinder.isAccessGranted(ctx) },
				onActivate = { ctx ->
					ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
						addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					})
				}
			),
			GuideStep(
				id = "battery_aosp",
				title = "Bateria sin restricciones",
				promptMessage = "Selecciona 'Sin restricciones' o pulsa Permitir para que el sistema no suspenda el servicio.",
				isDone = { ctx -> BatteryOptimizer.isIgnoring(ctx) },
				onActivate = { ctx -> BatteryOptimizer.requestIgnore(ctx) }
			),
			GuideStep(
				id = "autostart",
				title = "Inicio automatico (Xiaomi)",
				promptMessage = "Activa el switch de inicio automatico para NotifHook.",
				isDone = { ctx -> GuidePreferences.isAutoStartDone(ctx) },
				onActivate = { ctx ->
					GuidePreferences.setAutoStartDone(ctx, true)
					XiaomiHelper.openAutoStart(ctx)
				}
			),
			GuideStep(
				id = "miui_battery",
				title = "Ahorro bateria MIUI (Xiaomi)",
				promptMessage = "En Ahorro de bateria de la aplicacion, selecciona la opcion 'Sin restricciones'.",
				isDone = { ctx -> GuidePreferences.isMiuiBatteryDone(ctx) },
				onActivate = { ctx ->
					GuidePreferences.setMiuiBatteryDone(ctx, true)
					XiaomiHelper.openBatterySaver(ctx)
				}
			),
			GuideStep(
				id = "lock_recents",
				title = "Candado en recientes (Xiaomi)",
				promptMessage = "Abre la multitarea de recientes, manten pulsada la tarjeta de NotifHook y toca el icono del candado.",
				isDone = { ctx -> GuidePreferences.isLockRecentsDone(ctx) },
				onActivate = { ctx ->
					GuidePreferences.setLockRecentsDone(ctx, true)
				}
			)
		)

		val container = LinearLayout(activity).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(32, 16, 32, 16)
		}

		val subtitle = TextView(activity).apply {
			text = "Pasos para evitar que HyperOS / MIUI o Android detengan la aplicacion en segundo plano:"
			textSize = 14f
			setPadding(0, 0, 0, 16)
		}
		container.addView(subtitle)

		val stepsContainer = LinearLayout(activity).apply {
			orientation = LinearLayout.VERTICAL
		}
		container.addView(stepsContainer)

		val scrollView = ScrollView(activity).apply {
			addView(container)
		}

		fun populateSteps() {
			stepsContainer.removeAllViews()
			steps.forEachIndexed { index, step ->
				val done = step.isDone(activity)
				val row = LinearLayout(activity).apply {
					orientation = LinearLayout.HORIZONTAL
					gravity = Gravity.CENTER_VERTICAL
					setPadding(0, 12, 0, 12)
				}

				val textColumn = LinearLayout(activity).apply {
					orientation = LinearLayout.VERTICAL
				}

				val statusLabel = TextView(activity).apply {
					text = if (done) "[✓] Hecho" else "[!] Pendiente"
					setTextColor(if (done) Color.parseColor("#008800") else Color.parseColor("#CC4400"))
					textSize = 12f
					typeface = Typeface.DEFAULT_BOLD
				}

				val titleLabel = TextView(activity).apply {
					text = step.title
					textSize = 14f
					setTextColor(Color.DKGRAY)
				}

				textColumn.addView(statusLabel)
				textColumn.addView(titleLabel)

				val actionButton = Button(activity).apply {
					text = if (done) "Revisar" else "Activar"
					textSize = 12f
					setOnClickListener {
						AlertDialog.Builder(activity)
							.setTitle(step.title)
							.setMessage(step.promptMessage)
							.setPositiveButton("OK") { _, _ ->
								step.onActivate(activity)
								populateSteps()
							}
							.setNegativeButton("Cancelar", null)
							.show()
					}
				}

				val textParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
				val buttonParams = LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
				).apply {
					setMargins(16, 0, 0, 0)
				}

				row.addView(textColumn, textParams)
				row.addView(actionButton, buttonParams)
				stepsContainer.addView(row)

				if (index < steps.size - 1) {
					val divider = View(activity).apply {
						setBackgroundColor(Color.LTGRAY)
					}
					stepsContainer.addView(divider, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1))
				}
			}
		}

		populateSteps()

		AlertDialog.Builder(activity)
			.setTitle("Optimizacion en segundo plano")
			.setView(scrollView)
			.setPositiveButton("Cerrar") { dialog, _ ->
				dialog.dismiss()
				onDismiss()
			}
			.setOnDismissListener { onDismiss() }
			.show()
	}
}
