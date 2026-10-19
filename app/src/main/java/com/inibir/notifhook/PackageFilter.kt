package com.inibir.notifhook

object PackageFilter {

	fun accepts(packagesText: String, packageName: String): Boolean {
		val allowed = packagesText.lines().map { it.trim() }.filter { it.isNotEmpty() }
		return allowed.isEmpty() || packageName in allowed
	}
}
