# Project Status - NotifHook v1.2.0

## Arquitectura: Android Service Architecture (Service / Storage / Network)
Servicio en segundo plano de Android para interceptar notificaciones del sistema y reenviarlas mediante webhooks HTTP con persistencia offline.

## Mapeo de Modulos y Responsabilidades
### `app/src/main/java/com/inibir/notifhook`
- `NotifListener`: Servicio NotificationListenerService que intercepta notificaciones entrantes de apps autorizadas.
- `ServiceRebinder`: Revinculacion forzada del NotificationListenerService y verificacion de permisos del sistema.
- `BatteryOptimizer`: Verificacion y solicitud de exclusion de optimizaciones de bateria para evitar suspension del proceso.
- `BootReceiver`: Receptor de arranque del dispositivo y actualizacion del paquete para reactivar el listener y la sincronizacion.
- `NotificationPayload::from(sbn: StatusBarNotification) -> JSONObject`: Extrae titulo, texto, paquete y timestamp de la notificacion.
- `NotificationStore`: Persistencia local de notificaciones pendientes en almacenamiento local.
- `Outbox`: Gestion de cola y sincronizacion de notificaciones hacia el webhook remoto.
- `HttpSender::send(url: String, payload: JSONObject) -> Boolean`: Envio de peticiones HTTP POST con el payload JSON.
- `AppRegistry`: Control de aplicaciones instaladas y estado de habilitacion para interceptacion.
- `HookConfig`: Lectura y almacenamiento de la URL destino del webhook.
- `MainActivity`: Interfaz grafica principal para configuracion de URL, estado del servicio, bateria y permisos.
- `AppsActivity`: Interfaz para activar o desactivar la escucha por paquete de aplicacion.
- `SyncJobService`: Tarea periodica de reintentos para entrega de notificaciones encoladas.
- `SyncScheduler`: Planificador de tareas JobScheduler para ejecucion de sincronizaciones en background.

## Endpoints / Interfaces Externas (si aplica)
- `[POST] {webhook_url}` [Auth: Public / Configurable]:
	- **Params / Payload**: `{ package: String, title: String, text: String, time: Long }`
	- **Response**: `{ status: 200 }`
	- **Descripcion**: Endpoint HTTP externo configurado por el usuario donde se despachan las notificaciones capturadas.
