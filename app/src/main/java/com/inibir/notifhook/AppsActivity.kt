package com.inibir.notifhook

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView

class AppsActivity : Activity() {

	private lateinit var adapter: AppsAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		adapter = AppsAdapter(this, AppRegistry(this))

		val info = TextView(this).apply {
			text = "Apps que han mostrado notificaciones. Marca las que quieres enviar."
		}

		val search = EditText(this).apply {
			hint = "Buscar"
			addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
				override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
				override fun afterTextChanged(s: Editable?) = adapter.filter(s.toString())
			})
		}

		val list = ListView(this).apply { adapter = this@AppsActivity.adapter }

		val root = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 24)
			addView(info)
			addView(search)
			addView(list, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
		}

		setContentView(root)
	}

	override fun onResume() {
		super.onResume()
		adapter.reload()
	}
}
