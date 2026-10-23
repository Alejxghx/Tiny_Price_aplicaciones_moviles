# Configuración remota y transición segura

Proyecto: `ddzgaqjsoxsohkrsoowj` (Tiny_Price), URL `https://ddzgaqjsoxsohkrsoowj.supabase.co`.

## Estado verificado

El laboratorio original funciona: `search-products` versión 4 devolvió HTTP 200 con seis fuentes reales. El secreto TinyFish fue guardado por el propietario. Las nuevas migraciones de cuotas están aplicadas y probadas; los cambios de autenticación todavía requieren activar el proveedor antes de sustituir esa función.

## Activación necesaria del propietario

En [Authentication → Sign In / Providers](https://supabase.com/dashboard/project/ddzgaqjsoxsohkrsoowj/auth/providers), habilitar **Anonymous Sign-Ins** y guardar. El proveedor figura deshabilitado en `/auth/v1/settings` al momento de preparar la entrega. La integración autorizada no expone una operación para cambiarlo, y Brave no está conectado a Computer Use.

La clave TinyFish fue mostrada en el chat/captura. Crear una nueva en TinyFish, guardarla como `TINYFISH_API_KEY` en Supabase → Edge Functions → Secrets y revocar la anterior. No pegarla aquí. No se puede dar por rotada sin comprobarlo.

## Desplegar la versión protegida

Con sesión CLI autorizada (o usando la integración de Supabase), después de activar el proveedor:

```sh
npm ci
npm run supabase -- login
npm run supabase -- link --project-ref ddzgaqjsoxsohkrsoowj
npm run supabase -- db push
npm run supabase -- functions deploy delete-account --project-ref ddzgaqjsoxsohkrsoowj
npm run supabase -- functions deploy search-products --project-ref ddzgaqjsoxsohkrsoowj
```

Las dos migraciones de esta entrega ya se aplicaron en el proyecto. Antes de usar `db push`, revisar que el historial remoto y local coincidan. No reaplicar SQL manualmente ni borrar tablas.

`verify_jwt = false` desactiva únicamente la validación heredada del gateway; el código de cada función exige un token Bearer y lo valida en Supabase Auth. Una petición de búsqueda válida sin sesión debe devolver 401. No desplegar una versión que omita esa validación.

## Verificación final necesaria

Crear una única sesión anónima mediante `/auth/v1/signup` con la clave publicable del cliente; no registrar tokens. Buscar con `Authorization: Bearer <token>` y esperar 200. Verificar 401 sin token, 400 para consulta inválida y 204 para OPTIONS. Después enviar DELETE a `/functions/v1/delete-account` con el mismo token y comprobar 204 y rechazo posterior del token. Finalmente recorrer la app firmada, renovar sesión y eliminar los datos desde Privacidad.

Los límites están en `private.tinyprice_limits`: 10 por minuto, 50 por día por sesión, 500 globales por día UTC. Para pausar: `update private.tinyprice_limits set enabled = false where id = true;` desde el SQL Editor autorizado. Solo el servidor puede reservar cuotas; no dar acceso de clientes a las tablas privadas ni a los RPC.

## Compilar y distribuir

```sh
./gradlew :app:assembleDebug -PuseRemoteDeals=true
./gradlew :app:assembleRelease :app:bundleRelease
```

La firma requiere los archivos privados entregados por separado. El propietario confirmó que no tiene Play Console; no existe aún una pista interna. Ver `PUBLICACION.md`.
