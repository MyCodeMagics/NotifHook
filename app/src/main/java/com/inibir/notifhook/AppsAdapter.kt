package com.inibir.notifhook

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox

class AppsAdapter(
	private val context: Context,
	private val registry: AppRegistry
) : BaseAdapter() {

	data class Entry(val packageName: String, val label: String)

	private var all: List<Entry> = emptyList()
	private var shown: List<Entry> = emptyList()
	private var query = ""

	fun reload() {
		all = (registry.seenPackages() + InstalledApps.launchable(context))
			.distinct()
			.map { Entry(it, AppLabel.of(context, it)) }
			.sortedWith(compareBy({ !registry.isEnabled(it.packageName) }, { it.label.lowercase() }))
		applyFilter()
	}

	fun filter(text: String) {
		query = text.trim().lowercase()
		applyFilter()
	}

	private fun applyFilter() {
		shown = all.filter {
			query.isEmpty() || it.label.lowercase().contains(query) || it.packageName.contains(query)
		}
		notifyDataSetChanged()
	}

	override fun getCount() = shown.size

	override fun getItem(position: Int) = shown[position]

	override fun getItemId(position: Int) = position.toLong()

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val entry = shown[position]
		val row = (convertView as? CheckBox) ?: CheckBox(context).apply { setPadding(24, 24, 24, 24) }

		row.setOnCheckedChangeListener(null)
		row.text = "${entry.label}\n${entry.packageName}"
		row.isChecked = registry.isEnabled(entry.packageName)
		row.setOnCheckedChangeListener { _, checked -> registry.setEnabled(entry.packageName, checked) }
		return row
	}
}
