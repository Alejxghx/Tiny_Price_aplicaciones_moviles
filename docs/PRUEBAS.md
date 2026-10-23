# Pruebas y limitaciones verificadas

Fecha: 2026-10-22, America/Guayaquil.

## Android

Entorno comprobado: OpenJDK 25, Gradle Wrapper 9.6.0, Android Gradle Plugin 9.4.1, SDK Platform 37 y minSdk 23. Se mantiene bytecode Java 11.

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

- Compilación debug: correcta.
- Pruebas unitarias: 20 aprobadas, 0 fallidas. Incluyen validación, carga, resultados, errores, sugerencias, detalle/regreso, cancelación con consulta inválida, búsquedas consecutivas, mapeo remoto y manejo de errores JSON.
- Android Lint: correcto, sin errores. Se conservan 27 advertencias sobre versiones disponibles, recursos de la plantilla, compatibilidad de atributos de iconos y sugerencias menores. No se hicieron actualizaciones de dependencias aisladas fuera del conjunto del codelab.
- Pruebas instrumentadas: **8 aprobadas, 0 fallidas**, en Pixel 9 Pro API 35. Los fallos iniciales de arranque/autorización se resolvieron usando una partición de datos temporal en `work/`, sin borrar la del propietario.
```sh
./gradlew :app:connectedDebugAndroidTest
```

Las seis pruebas Compose cubren inicio, sugerencia → resultados → detalle → regreso, carga, vacío, error y búsqueda mediante teclado. También permanece la prueba de contexto que traía la plantilla. La revisión visual del APK remoto se registra al final de este documento.

## Función Supabase local

```sh
npm ci
npm run check
npm test
```

Comprobación de tipos Deno correcta. Diecisiete pruebas aprobadas sin red ni claves reales:

1. CORS y métodos HTTP.
2. JSON roto y tipos/longitudes de consulta inválidos.
3. Secreto ausente devuelve 503.
4. Búsqueda, normalización de consulta, EC/es, límite de diez y ausencia de clave en la respuesta.
5. Fuentes con protocolos inseguros o URL inválida.
6. Errores HTTP, transporte, JSON y contrato del proveedor devuelven 502 controlado.
7. Timeout aborta la petición.
8. Límite de veinte solicitudes por minuto y vencimiento de la ventana.
9. Resultados vacíos válidos.
10. Secreto compuesto solo por espacios se trata como ausente.
11. Una clave única dentro de un prompt copiado se extrae; configuraciones con claves distintas se rechazan.

## Supabase alojado

Proyecto: `ddzgaqjsoxsohkrsoowj` (`Tiny_Price`, ACTIVE_HEALTHY).
Función: `search-products`, versión 4, ACTIVE, desplegada mediante la integración Supabase.

Endpoint: https://ddzgaqjsoxsohkrsoowj.supabase.co/functions/v1/search-products

Comprobaciones HTTP realizadas contra el endpoint real:

- OPTIONS: 204, CORS presente.
- GET: 405, CORS presente.
- JSON roto: 400.
- Consulta vacía: 400.
- Consulta de un carácter: 400.
- Consulta de 201 caracteres: 400.
- Antes de guardar el secreto: consulta válida devolvía 503 `TINYFISH_NOT_CONFIGURED`.
- Después de guardar y normalizar el secreto: consulta válida devolvió **200 con seis fuentes web** y `searchedAt` válido.

Los logs del proyecto confirmaron siete invocaciones en `function_edge_logs` y catorce entradas de ejecución en `function_logs` durante la ventana de verificación.

Esto verifica despliegue, validación, secreto configurado y resultados reales de TinyFish. Se comprobó el mapeo de URLs y precio «Ver en fuente». Los estados 429 y 502 se comprobaron con proveedor simulado; no se provocaron consumos ni errores deliberados en la cuenta real.

## Guía de recorrido manual

Con la demo: abrir app, probar sugerencia Nintendo Switch OLED, ver tres ofertas ficticias, abrir detalle y regresar. Probar Audífonos para vacío, consulta vacía para validación y rotar el teléfono.

Con el secreto configurado y el APK remoto: buscar, abrir fuente HTTPS, regresar, cambiar consulta durante otra búsqueda, desconectar Internet, observar error legible, reconectar y reintentar.

## Credenciales

La clave de TinyFish visible en las capturas y en el mensaje de onboarding no se copió a los archivos del proyecto. Debe reemplazarse antes de configurarla en Supabase. El prompt de onboarding instala MCP/skills para asistentes; la app usa TinyFish Search desde la Edge Function y no necesita esa instalación global.

## Recorrido remoto observado en el emulador

- Se instaló el APK `TinyPrice-Supabase.apk` y el inicio muestra «BÚSQUEDA WEB EN VIVO».
- La sugerencia Nintendo Switch OLED activa carga y después muestra «6 resultados encontrados en la web».
- La primera fuente observada es Tía Ecuador. El detalle muestra fragmento, URL, estado del precio y botón de apertura.
- «Abrir fuente web» inicia Chrome correctamente. La página de la tienda no se evaluó: Chrome recién instalado muestra su configuración y aceptación de condiciones iniciales. No se aceptaron condiciones en nombre del propietario.
- Se guardaron y revisaron visualmente `evidencias/resultados-remotos.png`, `evidencias/detalle-remoto.png` y `evidencias/sin-conexion.png`.
- Al desactivar Wi-Fi/datos del emulador, la búsqueda mostró el mensaje de conexión y el botón Reintentar. Se restauraron ambas conexiones y, tras pulsar Reintentar con la conexión validada, reaparecieron los seis resultados reales.

## Ampliación de seguridad y firma

Se añadieron seis pruebas de sesiones: caché, renovación, refresh inválido, error temporal, concurrencia y borrado confirmado antes de limpiar. La octava prueba instrumentada verifica cifrado real con Android Keystore, almacenamiento fuera de backups y eliminación del fichero. Los informes XML registran 20 unitarias y 8 instrumentadas sin fallos.

Deno ahora ejecuta 17 pruebas: las once originales, tres de autenticación/cuota y tres de eliminación (exige sesión, solo identidad verificada, error sanitizado). Se comprueban tipos de las dos funciones.

`supabase/tests/quota.sql` se ejecutó en el proyecto real dentro de transacción con rollback. Se comprobaron topes por minuto/día, global entre usuarios, pausa, eliminación individual sin reiniciar global, y permisos denegados a anon/authenticated. La segunda migración corrigió el nombre de una variable SQL detectado por esas pruebas.

Supabase Advisors de seguridad devuelve lista vacía tras corregir permisos de `public.rls_auto_enable()` y políticas privadas. `delete-account` versión 1 está ACTIVE; OPTIONS 204, GET 405 y DELETE sin sesión 401 se verificaron en vivo. El borrado de un usuario real queda pendiente de activar Anonymous Sign-Ins.

APK y AAB release compilados. `apksigner verify` confirma firmas v1 y v2 y clave RSA de 3072 bits. SHA-256 del certificado: `62fcde7297f4cc87b9ab1fafb73a24165f3c1a72517d67ac0d465ef8266ff8a3`. La firma privada no se incluye en Git ni en el ZIP de fuentes.

La configuración Auth pública aún devuelve `anonymous_users:false`. Por eso no se sustituyó la función del laboratorio y no se declara probada la búsqueda autenticada en producción. El APK 1.1 debe distribuirse después de completar `CONEXION_REAL.md`.
