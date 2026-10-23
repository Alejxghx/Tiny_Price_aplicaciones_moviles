# TinyPrice

Aplicación Android del [codelab TinyPrice](https://epanchanaf.github.io/tinyprice-codelab/#step-0), adaptada a `com.alejx.tiny_price`. Incluye demo sin conexión, búsqueda web, carga, resultados, vacío, error, detalle y apertura de fuentes.

## Estado

El recorrido del laboratorio se verificó con búsquedas reales en Supabase y TinyFish. Esta entrega agrega sesiones de invitado cifradas, cuotas persistentes, eliminación de datos, firma de publicación y verificación continua.

**La ampliación de seguridad está preparada, pero su activación alojada depende de habilitar Anonymous Sign-Ins en Supabase.** Ese proveedor sigue deshabilitado al cierre de la preparación. No se declara probado el nuevo recorrido remoto completo. El endpoint del laboratorio sigue en versión 4 hasta completar la transición. El APK firmado 1.1 requiere habilitar el proveedor y desplegar las dos funciones nuevas antes de distribuirlo.

- [avanzado.md](avanzado.md): cambios, motivos y pruebas.
- [falta.md](falta.md): pendientes reales y dependencias externas.
- [Conexión](docs/CONEXION_REAL.md), [pruebas](docs/PRUEBAS.md), [publicación](docs/PUBLICACION.md) y [privacidad](docs/PRIVACIDAD.md).

El informe con doce capturas está en [docs/entrega/Informe_TinyPrice.docx](docs/entrega/Informe_TinyPrice.docx).

## Ejecutar

Abre esta carpeta en Android Studio con JDK 25, SDK Platform 37 y Android 6.0/API 23 o posterior. Se genera bytecode Java 11. Configura `ANDROID_HOME` o deja que Studio cree `local.properties` (ignorado por Git).

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

En Windows usa `gradlew.bat`. Debug muestra por defecto tres ofertas ficticias de Nintendo Switch OLED. «Audífonos» permite probar el estado vacío. No se abren dominios de ejemplo como si fueran tiendas reales.

Para activar la conexión remota después de completar su configuración:

```sh
./gradlew :app:assembleDebug -PuseRemoteDeals=true
```

Release siempre usa conexión remota. La URL de Supabase y su clave publicable son públicas; la clave TinyFish y las credenciales de servicio permanecen en el servidor. No copies la clave TinyFish a Kotlin ni al repositorio.

## Arquitectura y seguridad preparada

`Compose → DealsViewModel/StateFlow → DealsRepository → Supabase → TinyFish`

- Demo local: `PreviewDealsRepository`.
- Red: Retrofit/Gson, cancelación y errores controlados.
- Sesiones: Supabase Auth anónimo, renovación y almacenamiento AES/GCM con Android Keystore, excluido de backups.
- Servidor: `search-products` valida al usuario mediante Auth antes de reservar cuota y llamar a TinyFish.
- Postgres: límites atómicos de 10/minuto y 50/día por invitado, y 500/día globales (UTC), configurables en `private.tinyprice_limits`.
- Eliminación: `delete-account` solo borra al usuario autenticado y sus contadores; conserva el agregado global para que recrear la sesión no reinicie el presupuesto.

Una sesión de invitado puede recrearse y no acredita una identidad real. El límite global acota solicitudes a TinyFish, pero no garantiza un importe monetario: revisa precios y créditos del proveedor. Los resultados dicen «Ver en fuente», sin inventar precios.

## Servidor y verificación continua

```sh
npm ci
npm run check
npm test
```

Pruebas Deno sin red ni secretos. `supabase/tests/quota.sql` verifica límites, pausa, eliminación y permisos dentro de una transacción con rollback. Las migraciones aplicadas están en `supabase/migrations/`. GitHub Actions compila y revisa Android y ejecuta las pruebas locales de servidor sin credenciales.

## Firma y entrega

Los APK/AAB firmados y la copia privada de la firma se entregan por separado. `.signing/` y `keystore.properties` están ignorados. Nunca subas el respaldo de firma a GitHub ni a una release pública. Consulta [publicación](docs/PUBLICACION.md).

## Android CLI y Gemini

La skill oficial está en `.agents/skills/android-cli/SKILL.md`. Android CLI 1.0.16625370 se utilizó con el emulador. La sintaxis actual para instalar la skill es `android skills add android-cli --project=.`.

Gemini CLI 0.65.0 se instaló en la carpeta de herramientas local. La autenticación en Google y una ejecución con el modelo no se consideran completadas sin evidencia. La instalación no requiere incluir tokens en el proyecto.

## Referencia

Adaptado del [código oficial del codelab](https://github.com/epanchanaf/tinyprice-codelab/tree/main/reference), conservando sus capas y ampliando validación, cancelación, pruebas y protección del servidor.
