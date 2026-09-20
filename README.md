# SafeMotion
Safe Motion - Equipo 2 - Nexo Móvil.

Base Android para la primera entrega. Incluye una Activity, tema claro y oscuro, Navigation 3 y una pantalla inicial. Las demás pantallas están pendientes.

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
