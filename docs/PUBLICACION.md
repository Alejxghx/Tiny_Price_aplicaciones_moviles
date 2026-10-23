# Paquete preparado para publicación

Versión Android: 1.1 (código 2). Identificador: `com.alejx.tiny_price`. API mínima 23. El APK release y el AAB usan una clave de subida privada RSA de 3072 bits, separada del repositorio. Conserva el archivo privado de firma en un lugar seguro: las actualizaciones futuras deben usar la misma clave o el procedimiento de recuperación de Play App Signing.

## Ficha propuesta

- Nombre: TinyPrice
- Descripción breve: Encuentra fuentes web de productos y consulta sus ofertas en un solo lugar.
- Descripción: TinyPrice ayuda a buscar páginas de productos. Escribe lo que necesitas, revisa las fuentes encontradas y abre la tienda para confirmar precio, disponibilidad y entrega. Incluye una demostración sin conexión y búsqueda remota con TinyFish. Los resultados web no son precios garantizados ni recomendaciones patrocinadas.
- Categoría propuesta: Compras. Sin anuncios ni compras integradas en esta versión.
- Privacidad: https://alejxghx.github.io/Tiny_Price_aplicaciones_moviles/

## Pendientes que necesitan la cuenta del propietario

El propietario confirmó que todavía no tiene Play Console. Por tanto, no hay publicación en pista interna ni enlace de instalación de Google Play. Crear la cuenta, pagar su registro, verificar identidad y aceptar acuerdos son acciones del propietario. No se han realizado ni marcado como completas.

Después de crearla: crear app, seleccionar idioma español, revisar condiciones, activar Play App Signing y subir `TinyPrice-release.aab` a una pista interna. Completar clasificación de contenido, público objetivo, seguridad de datos, contacto del desarrollador y política de privacidad antes de enviar a revisión. El acceso de invitado debe estar habilitado en Supabase y la clave TinyFish expuesta debe rotarse antes de distribuir ampliamente.

## Información para revisar en Seguridad de datos

La app transmite consultas de búsqueda y utiliza un identificador de invitado y contadores. Supabase y TinyFish son proveedores externos; sus contratos y la definición vigente de recopilación/compartición de Google Play deben revisarse antes de contestar el formulario. No declarar “no se recopilan datos” automáticamente. Hay HTTPS y una opción de eliminación dentro de la app. La vía externa de ayuda está documentada en la política; verificar si cubre todos los requisitos de eliminación de cuentas antes de producción.

Referencias oficiales: [requisitos de eliminación de cuentas](https://support.google.com/googleplay/android-developer/answer/13327111), [Seguridad de datos](https://support.google.com/googleplay/android-developer/answer/10787469), [prueba interna](https://support.google.com/googleplay/android-developer/answer/9845334).

## Compilar una actualización

Restaurar de forma privada `.signing/tinyprice-upload.p12` y `keystore.properties` en la raíz del proyecto; nunca subirlos a Git. Incrementar `versionCode` y compilar `./gradlew :app:assembleRelease :app:bundleRelease`. En un clon sin firma, Gradle puede generar artefactos sin firmar; no publicarlos. La entrega de esta sesión sí se verificó con firma.
