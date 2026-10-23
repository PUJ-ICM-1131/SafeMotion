# SafeMotion
Safe Motion - Equipo 2 - Nexo Móvil.

Aplicación Android de acompañamiento para corredores y ciclistas. Esta versión corresponde a la Entrega 2: funciona para un usuario y mantiene datos de prueba para guardianes y alertas recibidas.

## Ejecutar
Abrir esta carpeta en Android Studio, instalar el SDK 37 y sincronizar Gradle. Ejecutar `app` en un emulador o dispositivo con Android 7.0 (API 24) o superior.

Para compilar: `./gradlew :app:assembleDebug` (Windows: `gradlew.bat :app:assembleDebug`). Usar el JDK de Android Studio y configurar el SDK en `local.properties` o `ANDROID_HOME`.

## Organización
- `ui/screens`: pantallas, ViewModel y UiState por funcionalidad.
- `ui/components`: componentes compartidos.
- `ui/theme`: colores y tipografía. Por ahora usamos la tipografía predeterminada de Material 3.
- `navigation`: rutas y navegación.
- `data/model`, `data/repository` y `data/sensor`: modelos, acceso a datos y lectura de sensores.

Configuración inicial: AGP 9.1.1, Kotlin 2.4.0, Gradle 9.3.1, Compose BOM 2026.08.00 y Navigation 3 1.0.1.

La propuesta, las historias y los diseños están en la [wiki](https://github.com/PUJ-ICM-1131/SafeMotion/wiki).

## Probar la app
Registra una cuenta con correo y contraseña o ingresa con una cuenta existente de Firebase. La recuperación de contraseña envía un correo real. No hay una cuenta de prueba fija.

Desde Inicio puedes abrir el mapa de riesgos, permitir la ubicación y crear un reporte con foto de cámara o galería. En Preparar recorrido, Planear ruta usa la ubicación actual y un destino elegido en el mapa; calcula rutas para correr o bicicleta con OSRM.

Para probar los sensores, selecciona al menos un guardián e inicia un recorrido. El acelerómetro y el giroscopio pueden activar una alerta de caída o inactividad; hay 30 segundos para cancelarla. Los botones de simulación permiten mostrar el flujo sin provocar una caída real.

El GPS, el tiempo y la distancia mostrados durante el recorrido activo siguen simulados; la ubicación real se usa en el mapa, los reportes y la planeación de rutas. Los guardianes, sus avisos y los reportes comunitarios no se sincronizan entre usuarios. La detección por sensores se ha probado con la app abierta, no con la pantalla bloqueada.

La implementación y los diagramas están en la [wiki](https://github.com/PUJ-ICM-1131/SafeMotion/wiki).
