

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

### 📝 Registro: [v1.2.0] - Reorganizacion de interfaz grafica en MainActivity e incorporacion de iconos
- **date-time**: 2026-10-23 04:22:00
- **Problema**:
	- Los botones no tenian iconos identificativos.
	- El boton de guardar no estaba situado junto al campo de URL.
	- Los botones de pruebas y configuracion no estaban estructurados por secciones claras.
- **Causa**:
	- La interfaz de MainActivity ubicaba todos los controles en una disposicion lineal vertical sin agrupamiento tematico ni drawables asignados.
- **Solución**:
	- Se coloco el campo de texto de URL y el boton Guardar en una fila horizontal `LinearLayout.HORIZONTAL` con icono de disquete (`android.R.drawable.ic_menu_save`).
	- Se anadio la seccion "Configuración" agrupando `Elegir aplicaciones` (`ic_menu_manage`), `Acceso a notificaciones` (`ic_menu_preferences`), `Reconectar servicio` (`ic_menu_rotate`) y `Desactivar ahorro de bateria` (`ic_menu_info_details`).
	- Se anadio la seccion inferior "Pruebas y Notificaciones" agrupando `Enviar notificacion de prueba` (`ic_menu_send`), el contador de pendientes y `Reenviar pendientes` (`ic_menu_upload`).