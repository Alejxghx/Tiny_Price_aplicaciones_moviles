# TinyPrice

Aplicación Android del [codelab TinyPrice](https://epanchanaf.github.io/tinyprice-codelab/#step-0), adaptada al repositorio de Alejxghx. Conserva `com.alejx.tiny_price` y el wrapper original.

## Estado de la entrega

La app incluye búsqueda demo, estados de carga/resultados/vacío/error, detalle, regreso y enlaces a fuentes web. El repositorio remoto está preparado y la Edge Function está activa en Supabase. **Búsqueda real verificada: Supabase devuelve HTTP 200 con fuentes de TinyFish. La función está activa en versión 4 y el secreto está configurado.**

- `avanzado.md`: cambios, motivos y resultados de verificación.
- `falta.md`: lista completa creada antes de implementar; las tareas externas siguen sin marcar.
- `docs/CONEXION_REAL.md`: pasos para conectar tus cuentas.
- `docs/PRUEBAS.md`: comandos y evidencia de calidad.

## Abrir y ejecutar la demo

Abre esta carpeta en Android Studio. Usa JDK 25 (el repositorio ya tiene ese criterio de daemon), SDK Platform 37 y un teléfono/emulador con Android 6.0/API 23 o posterior. El código genera bytecode Java 11.

Si compilas desde terminal, configura `ANDROID_HOME` con la ruta de tu SDK o deja que Android Studio genere `local.properties`. Ese archivo está ignorado por Git.

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

En Windows usa `gradlew.bat`. Para instalar, selecciona tu dispositivo en Android Studio y pulsa Run. El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

La demo tiene tres ofertas ficticias de Nintendo Switch OLED. Prueba la sugerencia de ese nombre; «Audífonos» y «Laptop gamer» permiten probar el estado vacío. Los importes y tiendas demo son ficticios. Sus dominios de ejemplo no se abren como fuentes reales.

## Arquitectura

`Compose → DealsViewModel/StateFlow → DealsRepository`

- `PreviewDealsRepository`: datos locales sin conexión.
- `RemoteDealsRepository`: Retrofit/Gson y corrutinas; llama únicamente a Supabase.
- `supabase/functions/search-products/index.ts`: arranque de la función.
- `supabase/functions/search-products/search.ts`: validación, límite por instancia, TinyFish y mapeo.

La app envía `POST /search-products` con `{"query":"Switch OLED"}`. La respuesta contiene `query`, `offers` y `searchedAt`. Se muestran hasta diez resultados. TinyFish Search no garantiza precios normalizados: los resultados reales dicen «Ver en fuente».

## Herramientas y pruebas del servidor

Usa Node.js 20 o superior:

```sh
npm ci
npm run check
npm test
npm run supabase -- --version
```

Las pruebas Deno se ejecutan sin credenciales ni acceso a Internet. El lockfile conserva las versiones instaladas. Deno y Supabase CLI son herramientas de desarrollo; no se incluyen en el APK.

## Activar el repositorio remoto

Después de desplegar y comprobar el endpoint, compila sustituyendo `YOUR_PROJECT_REF`:

```sh
./gradlew :app:assembleDebug \
  -PuseRemoteDeals=true \
  -PedgeRuntimeBaseUrl=https://YOUR_PROJECT_REF.supabase.co/functions/v1
```

La propiedad contiene la URL base; no agregues `/search-products`. Para regresar a la demo:

```sh
./gradlew :app:assembleDebug -PuseRemoteDeals=false
```

No se necesitan claves de Supabase en Android para esta función pública del laboratorio. `TINYFISH_API_KEY` se configura exclusivamente como secreto en Supabase.

## Android CLI y actividad opcional de Gemini

La skill oficial queda versionada en `.agents/skills/android-cli/SKILL.md`. En esta sesión Android CLI se preparó en la carpeta de trabajo, sin cambiar tu PATH global. Para instalarlo permanentemente consulta el instalador oficial enlazado por el codelab.

La sintaxis actual comprobada es:

```sh
android skills add android-cli --project=.
```

Con CLI 1.0.16625370 hay que indicar el nombre y no se generan el lockfile ni las referencias que describe la guía. Gemini CLI es opcional; su instalación/inicio de sesión y la activación de la skill en Gemini quedan como actividad pendiente del propietario.

## Referencia

La implementación se adaptó del [código completo del codelab](https://github.com/epanchanaf/tinyprice-codelab/tree/main/reference), conservando su separación de capas y agregando pruebas y correcciones de cancelación, validación y manejo de errores.
