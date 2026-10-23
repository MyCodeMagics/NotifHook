

### 📝 Registro: [v1.1.0] - Persistencia del servicio de notificaciones y visibilidad de NotifHook en lista de apps
- **date-time**: 2026-10-23 04:10:00
- **Problema**:
	- El servicio de escucha de notificaciones dejaba de recibir notificaciones de forma silenciosa tras reinicios o suspensión del sistema Android.
	- NotifHook no aparecía en la lista de "Elegir aplicaciones" (AppsActivity).
- **Causa**:
	- Android desvincula silenciosamente los NotificationListenerService por optimización agresiva de batería o presión de memoria RAM, sin revincularlos automáticamente al volver a abrir la app.
	- AppsAdapter excluía explícitamente a NotifHook mediante `.filter { it != context.packageName }`.
- **Solución**:
	- Se implementó `ServiceRebinder` para forzar el reinicio del componente en PackageManager y llamar a `NotificationListenerService.requestRebind()`.
	- Se implementó `BatteryOptimizer` para comprobar y solicitar la exclusión de optimización de batería (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
	- Se implementó `BootReceiver` con el permiso `RECEIVE_BOOT_COMPLETED` para asegurar que el servicio y la cola de sincronización revinculen tras reiniciar el móvil o actualizar la app.
	- Se actualizó `MainActivity` para mostrar el estado en tiempo real (acceso, conexión y batería), auto-revincular al abrir la app (`onResume`), y ofrecer botones directos de "Reconectar servicio" y "Desactivar ahorro de batería".
	- Se eliminó el filtro en `AppsAdapter` para que NotifHook aparezca en la lista de aplicaciones seleccionables.