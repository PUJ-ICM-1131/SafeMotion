# SafeMotion
Safe Motion - Equipo 2 - Nexo Móvil.

Base Android para la primera entrega. Incluye una Activity, tema claro y oscuro, Navigation 3, autenticación simulada, inicio y un recorrido de prueba con alerta. Guardianes, mapa, monitoreo e historial todavía necesitan sus pantallas.

## Ejecutar
Abrir esta carpeta en Android Studio, instalar el SDK 37 y sincronizar Gradle. Ejecutar `app` en un emulador o dispositivo con Android 7.0 (API 24) o superior.

Para compilar: `./gradlew :app:assembleDebug` (Windows: `gradlew.bat :app:assembleDebug`). Usar el JDK de Android Studio y configurar el SDK en `local.properties` o `ANDROID_HOME`.

## Organización
- `ui/screens`: pantallas por funcionalidad; aquí irán sus ViewModel y UiState.
- `ui/components`: componentes compartidos.
- `ui/theme`: colores y tipografía. Por ahora usamos la tipografía predeterminada de Material 3.
- `navigation`: rutas y navegación.
- `data/model` y `data/repository`: modelos y datos simulados.

Configuración inicial: AGP 9.1.1, Kotlin 2.4.0, Gradle 9.3.1, Compose BOM 2026.08.00 y Navigation 3 1.0.1.

La propuesta, las historias y los diseños están en la [wiki](https://github.com/PUJ-ICM-1131/SafeMotion/wiki).

## Probar el acceso
La cuenta de prueba es `mariana.rojas@correo.com` con contraseña `clave1234`. También se puede registrar una cuenta nueva mientras la app está abierta. Las cuentas nuevas se pierden al cerrar el proceso. La recuperación de contraseña solo muestra una confirmación; no envía correos.

## Probar el recorrido
Desde Inicio, entra a Preparar recorrido. Elige correr o ciclismo y al menos un guardián. El tiempo y la distancia son simulados. Puedes disparar una caída o inactividad de prueba, cancelar la alerta antes de 30 segundos o dejar que llegue a cero. Para terminar el recorrido se pide confirmación. Los recorridos y alertas confirmadas quedan en memoria mientras la app está abierta.

## Etapas de esta copia local
1. Datos y estado del recorrido simulado.
2. Preparación y seguimiento del recorrido.
3. Alerta de incidente y tiempo para cancelarla.
4. Navegación, pruebas y conexión con Inicio.

`MockRunRepository.availableGuardians` es una lista temporal: Juan Diego puede sustituirla al integrar su módulo. `completedRuns` y `confirmedIncidents` quedan disponibles para el historial y la alerta recibida que harán Samuel y el equipo. Aquí no hay GPS, sensores ni notificaciones reales.
