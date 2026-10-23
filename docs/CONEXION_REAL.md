# Conectar TinyPrice con búsquedas reales

Estado actualizado: proyecto `Tiny_Price` conectado, función `search-products` desplegada y ACTIVE (versión 4), secreto guardado por el propietario y búsqueda real HTTP 200 comprobada con seis fuentes. URL pública: `https://ddzgaqjsoxsohkrsoowj.supabase.co`.

El manejador acepta una clave sola o una clave única dentro de una etiqueta/prompt copiado. El texto jamás se ejecuta ni se registra; dos claves distintas se consideran configuración ambigua. Para futuras rotaciones, guarda únicamente la clave.

La captura compartida muestra una clave privada de TinyFish. El propietario debe revocarla, crear otra y guardarla exclusivamente en Supabase Secrets. No se copió esa clave al repositorio ni se ejecutó el prompt de instalación mostrado en la captura.

## 1. Crear TinyFish

Abre el registro oficial desde el enlace del paso 3 del [codelab](https://epanchanaf.github.io/tinyprice-codelab/#step-2). Ya creaste la cuenta: revisa los créditos y genera una API key nueva. Guárdala en un gestor de contraseñas. No la pegues en el chat, Kotlin ni Git.

## 2. Crear el proyecto Supabase

Entra al [Dashboard de Supabase](https://supabase.com/dashboard), selecciona tu organización y crea un proyecto llamado `tinyprice`. Usa el plan que hayas elegido, una región apropiada y una contraseña de base de datos que guardes en tu gestor. Espera a que el proyecto esté disponible.

Copia el Project Ref desde los ajustes o la URL del Dashboard. Es el identificador público que sustituye `ddzgaqjsoxsohkrsoowj`. La app no usa tablas ni requiere migraciones SQL.

## 3. Guardar el secreto

En el proyecto abre **Edge Functions → Secrets**. Agrega el nombre `TINYFISH_API_KEY` y el valor que generaste en TinyFish. Guarda el secreto allí. No agregues la contraseña del Dashboard ni la de base de datos al proyecto Android.

## 4. Despliegue (ya completado en esta sesión)

Los comandos siguientes quedan como alternativa para futuros despliegues desde tu terminal:

Desde la raíz de este repositorio:

```sh
npm ci
npm run supabase -- login
npm run supabase -- link --project-ref ddzgaqjsoxsohkrsoowj
npm run supabase -- functions deploy search-products --project-ref ddzgaqjsoxsohkrsoowj
```

El inicio de sesión necesita tu autorización en el navegador. La configuración `supabase/config.toml` conserva `verify_jwt = false`, como exige la función pública del laboratorio.

Para verificar que el secreto existe, sin imprimir su valor:

```sh
npm run supabase -- secrets list --project-ref ddzgaqjsoxsohkrsoowj
```

## 5. Comprobar el endpoint alojado

```sh
curl --request POST \
  'https://ddzgaqjsoxsohkrsoowj.supabase.co/functions/v1/search-products' \
  --header 'Content-Type: application/json' \
  --data '{"query":"Nintendo Switch OLED"}'
```

Debe devolver 200 y un objeto con `offers` y `searchedAt`. Una lista vacía puede ser válida. Los campos de precio dicen «Ver en fuente»; abre el enlace para comprobarlos.

Comprueba también consulta vacía (400), consulta de un carácter (400), más de 200 caracteres (400), JSON roto (400), GET (405) y OPTIONS (204). Sin secreto, una consulta válida devuelve 503 con `TINYFISH_NOT_CONFIGURED`. Un fallo del proveedor devuelve 502. El límite didáctico permite veinte solicitudes por minuto y devuelve 429 al excederlo en la misma instancia.

Revisa **Edge Functions → search-products → Invocations/Logs**. Evita registrar cuerpos de solicitud, claves o cabeceras privadas. La función entrega errores controlados y no registra respuestas internas de TinyFish.

## 6. Compilar Android contra Supabase

```sh
./gradlew :app:assembleDebug \
  -PuseRemoteDeals=true \
  -PedgeRuntimeBaseUrl=https://ddzgaqjsoxsohkrsoowj.supabase.co/functions/v1
```

Instala el APK resultante y recorre búsqueda, carga, resultados, detalle, fuente y regreso. Prueba sin conexión: debe aparecer un error legible y permitir reintentar. Cambia a otra consulta antes de terminar una búsqueda para comprobar que los resultados anteriores no se imponen.

Puedes guardar estas dos propiedades públicas en tu configuración Gradle personal para ejecutar en modo remoto desde Android Studio. No guardes ahí la clave TinyFish.

## 7. Cerrar los pendientes

Después de obtener evidencia real, actualiza las casillas correspondientes de `falta.md` y registra qué comprobaste en `avanzado.md`. Para practicar rotación, genera otra clave en TinyFish, actualiza el secreto Supabase, realiza una búsqueda de control y revoca la clave anterior.

La publicación en Play Console es una fase posterior: necesita cuenta, clave de firma propia, AAB, ficha de aplicación y revisión de privacidad. No se generó una firma de publicación ni se distribuyó la aplicación en una tienda.

El endpoint público y su límite en memoria reproducen el laboratorio. Antes de distribuirlo ampliamente, completa autenticación, cuotas compartidas y protección de gasto.
