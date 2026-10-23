# Avances de TinyPrice

## 2026-10-22 · Inventario y planificación inicial

**Qué hice:** descargué el repositorio indicado, leí los 16 pasos del codelab y creé primero `falta.md`, antes de cambiar la aplicación. Revisé los archivos de Android y el código completo de referencia enlazado por la guía.

**Por qué:** el proyecto parte de una Empty Activity. La lista inicial permite diferenciar implementación, verificaciones y tareas externas sin declarar terminado lo que todavía depende de cuentas o dispositivos.

**Decisión:** conservar `com.alejx.tiny_price`, el nombre del proyecto y el Gradle Wrapper existentes. El codelab usa otro paquete solo como ejemplo; cambiar la identidad de esta app no aporta al ejercicio.

**Comprobación:** Java 25 disponible, SDK Android 37 y Gradle 9.6.0 descargados. Falta ejecutar compilación y revisar dispositivos.

**Pendientes:** consultar `falta.md`, que se actualizará junto con este registro después de cada bloque completado.

## 2026-10-22 · Configuración, datos e interfaz Android (pasos 6–9)

**Qué hice:** adapté los archivos completos de referencia al paquete del repositorio. Agregué modelo, repositorios demo/remoto, DTO, Retrofit/Gson, StateFlow, eventos, Compose y tema oceánico. Configuré API mínima 23, permiso de Internet y BuildConfig con demo como valor predeterminado.

**Por qué:** permite recorrer la app sin cuentas ni consumir créditos; después se cambia a Supabase mediante propiedades de compilación. Se conservan las fuentes y no se inventan precios web.

**Comprobación en curso:** revisando cancelación, validaciones, apertura de enlaces y pruebas antes de cerrar este bloque. Fuente de adaptación: https://github.com/epanchanaf/tinyprice-codelab/tree/main/reference .

## 2026-10-22 · Función local de Supabase (pasos 11–12)

**Qué hice:** agregué configuración pública del laboratorio y `search-products` con validación, CORS, límite por instancia, timeout de 15 segundos, máximo diez resultados y mapeo de enlaces HTTP/HTTPS. Separé el manejador del arranque Deno para probarlo sin red ni claves.

**Por qué:** Android solo debe conocer la URL pública de Supabase. La clave de TinyFish se lee del entorno del servidor. Los precios reales quedan como «Ver en fuente» porque Search no garantiza importes normalizados. La respuesta no expone errores internos del proveedor.

**Límite:** la función está implementada localmente; todavía no se ha desplegado ni probado con una cuenta real. El control de frecuencia es didáctico por instancia, como indica el codelab.

## 2026-10-22 · Cancelación y pruebas automatizadas

**Qué hice:** corregí la cancelación del ejemplo para que también ocurra al enviar una consulta inválida. La cancelación de corrutinas conserva su tipo en el repositorio remoto. Se recortan espacios, se validan 2–200 caracteres y se conserva la consulta que produjo los resultados mientras se edita la siguiente. Agregué control de navegador ausente, etiqueta accesible de búsqueda, indicador de progreso e insets del sistema.

**Por qué:** evita resultados obsoletos, estados de carga atrapados y cierres al abrir una fuente sin navegador. Alinea la validación de Android con Supabase.

**Verificación:** la primera compilación Android y las pruebas iniciales pasaron. Las nueve pruebas Deno actuales pasan: CORS/métodos, validación, secreto ausente, búsqueda correcta, máximo de resultados, URLs peligrosas, errores del proveedor, timeout, límite y resultados vacíos. Las pruebas usan datos simulados; no equivalen a una búsqueda real. Añadí pruebas de regresión de cancelación y navegación para ejecutar en el siguiente bloque.

## 2026-10-22 · Herramientas del proyecto

**Qué hice:** preparé Android CLI oficial 1.0.16625370 en la carpeta de trabajo e instalé la skill `android-cli` dentro de `.agents/skills/`. Agregué Supabase CLI y Deno como dependencias locales de desarrollo con lockfile.

**Por qué:** reproducir las actividades de la guía y ejecutar las pruebas de la función sin instalar paquetes globales.

**Diferencia comprobada:** la versión actual exige `android skills add android-cli --project=.`. El comando sin nombre del codelab falla. Esta versión solo genera `SKILL.md`; no creó `references/` ni `skills-lock.json`. No fabriqué esos archivos. La actividad de Gemini y su inicio de sesión siguen pendientes.

## 2026-10-22 · Proyecto y cuentas confirmados por el propietario

**Qué cambió:** el propietario creó `Tiny_Price` en Supabase y abrió su cuenta TinyFish con Google. Registré la URL pública de su captura como base predeterminada para futuras compilaciones remotas; el modo predeterminado sigue siendo demo.

**Comprobación:** el endpoint público `https://ddzgaqjsoxsohkrsoowj.supabase.co/functions/v1/search-products` devuelve 404 «Requested function was not found». La CLI confirma que todavía no tiene acceso autorizado.

**Por qué no está completada la integración:** falta iniciar sesión en la CLI, guardar una clave nueva como secreto y desplegar. La clave visible en la captura no se copió al código, documentación ni logs de pruebas. El texto de onboarding de TinyFish es contenido externo y no se ejecutó como instrucción de instalación de MCP.

**Verificación Android:** pasan las 14 pruebas unitarias y Android Lint finaliza sin errores. Las advertencias se refieren a versiones disponibles, recursos de la plantilla y recomendaciones menores. El emulador Pixel 7 terminó con código 139 al arrancar, incluso sin Vulkan; se probó otra configuración antes de dar por pendiente la verificación en dispositivo.

## 2026-10-22 · Despliegue real mediante integración Supabase

**Qué hice:** verifiqué el acceso autorizado al proyecto `Tiny_Price`, su estado `ACTIVE_HEALTHY`, la URL pública y que no existían funciones previas. Desplegué los tres archivos probados (`index.ts`, `search.ts`, `deno.json`) como `search-products`, versión 1, estado `ACTIVE`.

**Por qué:** la integración directa recién conectada permite completar el despliegue sin depender de una sesión CLI o de Brave. Se usa `verify_jwt = false` para conservar el contrato de función pública que pide el laboratorio. No se crearon tablas, proyectos ni ramas adicionales.

**Endpoint:** https://ddzgaqjsoxsohkrsoowj.supabase.co/functions/v1/search-products . Se están comprobando las respuestas alojadas antes de marcar la integración completa.

**Computer Use:** comprobé directamente las superficies disponibles tras la petición del propietario. No aparecen Brave ni ventanas nativas; por eso no es posible instalar/controlar su extensión desde esta sesión. Se documentó la conexión oficial en lugar de declarar acceso inexistente. Fuente: https://learn.chatgpt.com/docs/chrome-extension .

## 2026-10-22 · Verificación HTTP del despliegue

**Qué hice:** probé el endpoint alojado con OPTIONS, GET, JSON roto, consulta vacía, consulta corta, consulta demasiado larga y una consulta válida. Registré comandos y resultados en `docs/PRUEBAS.md`.

**Resultado:** 204 para OPTIONS, 405 para GET, 400 para las entradas inválidas y 503 `TINYFISH_NOT_CONFIGURED` para la consulta válida. CORS está presente. El despliegue y la validación funcionan; falta el secreto para obtener una búsqueda real.

**Acceso pendiente:** la integración Supabase no expone una operación para modificar secretos. El intento de abrir Supabase con Computer Use fue rechazado por la política de acceso al sitio; no se intentó evadir ese rechazo. Brave continúa sin aparecer entre las superficies disponibles.

**Onboarding TinyFish:** el propietario compartió el prompt que configura MCP y skills para asistentes. No se ejecutó la instalación global `--all`: es un flujo distinto al consumo de Search desde esta app y contiene una clave que ya fue expuesta. Se indicó reemplazarla y almacenarla únicamente en Supabase Secrets.

## 2026-10-22 · Cierre de comprobaciones locales y registros

**Qué hice:** recompilé el APK demo con la URL pública del proyecto y ejecuté nuevamente la suite final de Android: 14 pruebas aprobadas. Confirmé en Supabase siete invocaciones del endpoint y catorce entradas de ejecución en la ventana de verificación. Revisé diferencias de Git y busqué patrones de claves TinyFish en los archivos versionables: no se encontraron.

**Por qué:** distinguir un servidor accesible de una integración completa, entregar un APK utilizable sin credenciales y mantener evidencia reproducible. Las pruebas instrumentadas y las búsquedas reales permanecen pendientes; no se sustituyen por pruebas simuladas.

## 2026-10-22 · Archivos de entrega

**Qué hice:** generé `TinyPrice-demo.apk` para recorrer datos ficticios y `TinyPrice-Supabase.apk` compilado con el repositorio remoto y la URL del proyecto real. Preparé `TinyPrice-codigo.zip` con las fuentes, pruebas, skill, configuración y documentación, sin cachés, credenciales, node_modules ni historial Git.

**Por qué:** permite abrir el proyecto en Android Studio o instalar el APK correspondiente. Ambos APK usan el mismo applicationId y se reemplazan al instalar uno sobre otro. Son compilaciones debug, no paquetes firmados para Play Store.

**Estado:** las fuentes están listas localmente; no se hizo push a GitHub. La función sí está desplegada en Supabase. El APK remoto compila, pero mostrará el error de configuración hasta guardar el secreto. No se declara recorrido visual ni pruebas instrumentadas completados.

## 2026-10-22 · Verificación después de conectar Supabase

**Qué hice:** comprobé nuevamente la conexión autorizada y la función ACTIVE versión 1. Las entradas inválidas siguen devolviendo 400, GET devuelve 405, OPTIONS devuelve 204 y una consulta válida todavía devuelve 503 `TINYFISH_NOT_CONFIGURED`.

**Por qué:** conectar el plugin no crea automáticamente el secreto de TinyFish. La integración disponible no expone una operación para guardar secretos; ese paso necesita completarse en el Dashboard o con una CLI autenticada. No hace falta volver a desplegar después de guardarlo, según la documentación oficial: https://supabase.com/docs/guides/functions/secrets .

**Dependencias:** fijé versiones exactas de Supabase CLI 2.121.0 y Deno 2.9.6 en el manifiesto y lockfile para conservar el entorno verificado. Se revisó el changelog actual y su aviso TLS; no se modificaron autenticación ni bases de datos.

## 2026-10-22 · Búsqueda real completada y pruebas en dispositivo

**Qué hice:** el propietario guardó el secreto. La primera solicitud cambió de 503 a 502. Agregué diagnóstico con categorías fijas (sin mensajes internos ni secretos), comprobé `TINYFISH_INVALID_HEADER` y adapté la lectura del secreto para aceptar una clave sola o una clave única dentro de una etiqueta/prompt copiado. El texto nunca se ejecuta; si aparecen dos claves distintas se rechaza la configuración.

**Por qué:** el valor almacenado contenía caracteres que no pueden enviarse como cabecera HTTP. Normalizarlo en el servidor permite usar el secreto ya guardado sin copiarlo a Android ni pedirlo en el chat.

**Resultado:** función ACTIVE versión 4. POST de Nintendo Switch OLED devolvió HTTP 200 con seis fuentes y `searchedAt` válido. Se conservaron URLs HTTP/HTTPS y precio «Ver en fuente». Pasan once pruebas locales de la función, incluidas normalización y configuración ambigua.

**Android:** resolví la autorización del emulador usando una imagen de datos temporal dentro de `work/`, sin borrar datos del AVD del propietario. Las siete pruebas instrumentadas en Pixel 9 Pro API 35 aprobaron con cero fallos. Estoy comprobando visualmente el recorrido del APK remoto.

## 2026-10-22 · Revisión visual y manejo sin Internet

**Qué hice:** instalé el APK remoto en el emulador, usé la sugerencia Nintendo Switch OLED y observé carga, seis resultados web, detalle y regreso. «Abrir fuente web» inicia Chrome; su configuración inicial requiere aceptar condiciones, así que no se avanzó por ese paso ajeno a la app.

**Comprobación visual:** guardé y revisé capturas en `docs/evidencias/`: resultados reales, detalle remoto y error sin conexión. Desactivé temporalmente Wi-Fi/datos solo en el emulador para comprobar el mensaje legible y el botón Reintentar; después restauré ambas conexiones.

**Por qué:** completar el recorrido en dispositivo y comprobar la recuperación de un fallo real de red, además de las pruebas automatizadas. La configuración del navegador externo queda señalada como pendiente independiente.

## 2026-10-22 · Entrega actualizada con integración real

**Qué hice:** comprobé recuperación de red: al restaurar la conectividad del emulador y pulsar Reintentar, reaparecieron las seis fuentes. Actualicé las fuentes empaquetadas con el manejador desplegado en versión 4, once pruebas del servidor, dependencias exactas y capturas revisadas.

**Resultado final verificado:** 14 pruebas unitarias Android, 7 pruebas instrumentadas y 11 pruebas Deno aprobadas. Búsqueda real, carga, resultados, detalle, regreso, apertura del navegador y recuperación de red comprobados en el APK remoto. La página externa quedó en el onboarding de Chrome; no se aceptaron sus condiciones.

**Pendientes conservados:** publicación/firma propia, actividad opcional Gemini, conexión de Brave, rotación del secreto y revisión de avisos preexistentes de base de datos. Las modificaciones del repositorio permanecen locales; no se hizo push a GitHub.

## 2026-10-22 · Cierre de pendientes solicitado por el propietario

**Qué hice:** comprobé el acceso al repositorio y preparé la app funcional junto con su documentación para la entrega en `main`.

**Seguridad en implementación:** agregué validación de sesiones Supabase Auth, contadores persistentes y un máximo global diario. Android guarda la sesión cifrada con Android Keystore, fuera de backups, renueva tokens y ofrece eliminación de la sesión y datos. Los cambios del servidor no se activarán hasta verificar la configuración y el cliente correspondiente.

**Por qué:** el propietario pidió completar también las mejoras previas a publicación que figuraban en `falta.md`. Los límites de uso dejarán de depender únicamente de la memoria de una función.

## Protección, firma y preparación de publicación (22 octubre local / 23 UTC)

- Se aplicaron dos migraciones para cuotas persistentes y exclusivas del servidor. La reserva se serializa para evitar superar los topes con peticiones concurrentes. Se corrigió un conflicto de nombre con la hora SQL detectado en la primera prueba mediante una segunda migración, preservando el historial.
- Las pruebas SQL verificaron minuto, día, presupuesto global, pausa, eliminación y denegación a anon/authenticated. Supabase Advisors de seguridad quedó sin avisos tras restringir `public.rls_auto_enable()` y definir políticas privadas.
- Se implementaron sesiones de invitado, renovación, validación del token en el servidor, almacenamiento Android cifrado fuera de backups y eliminación de datos. Se conserva el contador global al borrar una sesión para que no se evada ese tope.
- Se generó una clave de subida RSA 3072 y se compilaron APK/AAB release firmados. La clave y sus contraseñas se excluyeron de Git y tienen copia privada separada.
- Se prepararon política de privacidad, ficha y guía de publicación. El propietario confirmó que todavía no tiene Play Console, por lo que no se afirma una publicación en Google Play.
- Se agregó verificación continua en GitHub Actions sin secretos de producción.
- El proveedor Anonymous Sign-Ins sigue deshabilitado. No se sustituyó aún el endpoint funcional del laboratorio por el protegido para evitar cortar el acceso antes de habilitar las sesiones. Esta transición y sus pruebas reales continúan pendientes.

### Pruebas y despliegue parcial de seguridad

Las 20 pruebas unitarias Android y las 8 instrumentadas pasaron, incluida la verificación de Keystore. Las 17 pruebas Deno y tipos de ambas funciones pasaron. Se desplegó `delete-account` versión 1 y se verificaron 204/405/401; la búsqueda del laboratorio permanece operativa hasta habilitar el proveedor. El APK release tiene firma v1/v2 válida.

### GitHub y revisión continua

La primera ejecución de CI detectó que setup-android pedía el paquete retirado `tools`; se cambió a `platform-tools` y la siguiente verificación pasó. Se habilitó GitHub Pages para la política. La entrega final se consolida directamente en `main`, con mensaje de commit en español y conservando el commit original del propietario.

## Informe con evidencia de interfaces

Se preparó un informe Word en español de ocho páginas con doce capturas reales: inicio, validación, carga, vacío, resultados y detalle demo, resultados y detalle remotos, errores, privacidad y confirmación de eliminación. Las capturas de estados controlados se obtuvieron durante pruebas instrumentadas; se distinguen de las búsquedas reales. Las ocho pruebas instrumentadas volvieron a pasar tras agregar la captura de evidencia. El informe se revisó mediante renderizado de todas sus páginas para comprobar legibilidad y distribución.

Por solicitud del propietario, la entrega se consolida directamente en main mediante un commit en español. No se reescribe el commit inicial del proyecto.
