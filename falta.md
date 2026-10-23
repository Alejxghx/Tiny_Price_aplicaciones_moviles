# Pendientes de TinyPrice

Fuente: https://epanchanaf.github.io/tinyprice-codelab/#step-0
Fecha inicial: 2026-10-22 (America/Guayaquil).

Este archivo se creó antes de implementar cambios. Cada casilla representa trabajo pendiente; solo se marcará al completarlo. `avanzado.md` explicará qué se hizo, por qué y cómo se comprobó. Se mantienen ambos documentos en la raíz, con una entrada por tarea.

## Preparación (pasos 1–5)
- [x] Revisar el proyecto existente y conservar su identidad `com.alejx.tiny_price`.
- [x] Verificar Java 25, Gradle 9.6.0 y SDK 37.
- [x] Contar con emulador operativo: Pixel 9 Pro API 35 con datos temporales separados.
- [x] Instalar/verificar Android CLI y la skill oficial `android-cli` del proyecto.
- [x] Instalar Gemini CLI 0.65.0 en herramientas locales.
- [ ] Completar inicio de sesión Google y actividad opcional de activación de la skill en Gemini.
- [x] Crear cuenta TinyFish con Google (confirmado por el propietario).
- [ ] Revisar créditos y reemplazar la API key mostrada en la captura.
- [x] Probar TinyFish Search desde Supabase y comprobar su respuesta real.

## Aplicación Android (pasos 6–9)
- [x] Configurar dependencias, BuildConfig, modo demo/remoto, API mínima 23 y permiso de Internet.
- [x] Crear el modelo ProductOffer y el contrato DealsRepository.
- [x] Implementar datos ficticios identificados como demostración y búsqueda local.
- [x] Implementar DTO, Retrofit, tiempos de espera, mapeo y errores remotos.
- [x] Crear estado, eventos y ViewModel con StateFlow, validación y cancelación de búsquedas.
- [x] Conectar MainActivity con observación del ciclo de vida.
- [x] Crear tema oceánico y pantallas de inicio, carga, resultados, vacío, error y detalle.
- [x] Agregar sugerencias, búsqueda con teclado, navegación atrás, accesibilidad y apertura segura de fuentes.

## Supabase (pasos 10–13)
- [x] Crear/verificar cuenta Supabase y confirmar correo (confirmado por el propietario).
- [x] Crear proyecto alojado y registrar su referencia pública: `ddzgaqjsoxsohkrsoowj`.
- [x] Autorizar acceso al proyecto mediante la integración Supabase (la CLI ya no es requisito para este despliegue).
- [x] Agregar CLI de Supabase y configuración de función pública del laboratorio.
- [x] Implementar search-products: CORS, POST, JSON, consulta de 2–200 caracteres y límite didáctico.
- [x] Consultar TinyFish desde el servidor, con timeout, errores controlados y máximo diez ofertas.
- [x] Guardar TINYFISH_API_KEY como secreto de Supabase sin incluirla en Android/Git (guardado por el propietario y verificado con búsqueda real).
- [x] Desplegar `search-products` en Supabase: versión 4, estado ACTIVE.
- [x] Verificar respuestas alojadas 204/400/405 y 503 con secreto ausente.
- [x] Completar la primera búsqueda alojada HTTP 200: seis fuentes para Nintendo Switch OLED.
- [x] Revisar logs e invocaciones del endpoint alojado.
- [ ] Reemplazar la clave expuesta y verificar rotación con una búsqueda real.

## Integración, calidad y entrega (pasos 14–16)
- [x] Documentar ejecución local, despliegue y cambio al endpoint remoto.
- [x] Agregar pruebas unitarias de ViewModel y repositorio con datos controlados.
- [x] Agregar prueba instrumentada de búsqueda, detalle y regreso en Compose.
- [x] Agregar y ejecutar pruebas locales de la función con proveedor simulado.
- [x] Ejecutar compilación y 14 pruebas unitarias Android; registrar limitaciones de pruebas instrumentadas.
- [x] Ejecutar las siete pruebas instrumentadas en emulador: cero fallos.
- [x] Revisar visualmente resultados y detalle remoto; comprobar que abrir fuente inicia el navegador.
- [ ] Completar primera configuración del navegador del emulador para revisar la página externa de la tienda.
- [x] Configurar URL real y compilar el APK en modo remoto.
- [x] Verificar búsqueda, detalle, regreso y recuperación de red con Reintentar en el APK remoto.
- [x] Verificar ausencia de patrones de claves TinyFish en los archivos entregables y revisar cambios.
- [x] Probar manualmente el APK remoto con resultados reales tras configurar el secreto.
- [x] Preparar firma privada, APK y AAB release firmados.
- [ ] Publicar en pista interna: el propietario confirmó que no tiene cuenta de Play Console.

## Mejoras previas a producción (fuera del laboratorio básico)
- [x] Implementar autenticación de invitado y tokens cifrados en Android.
- [x] Aplicar migraciones y probar cuotas persistentes y tope global de gasto.
- [x] Implementar eliminación de cuenta/datos y desplegar `delete-account` versión 1.
- [ ] Habilitar Anonymous Sign-Ins (ajuste del propietario) y desplegar `search-products` protegido.
- [ ] Probar en vivo búsqueda autenticada, renovación y eliminación desde el APK firmado.
- [x] Preparar política de privacidad y ficha de publicación.
- [ ] Revisar con el propietario las declaraciones de datos, condiciones y contacto antes de enviar a Play.

## Dependencias externas
Las tareas de cuenta, secreto, proyecto remoto y publicación requieren acceso a los servicios del propietario. No se considerarán terminadas solo por escribir instrucciones o pruebas simuladas.

## Conexión de Brave solicitada por el propietario
- [ ] Habilitar la extensión oficial de navegador desde Ajustes → Computer Use → Brave.
- [ ] Verificar que Brave aparece en las herramientas antes de operar sus pestañas.

Computer Use se consultó directamente y solo devuelve el navegador interno y MCP Apps; no expone ventanas nativas. La conexión Supabase sí quedó autorizada y permitió desplegar sin depender de Brave.

## Entrega local
- [x] Preparar fuentes en `TinyPrice-codigo.zip`, APK demo y APK Supabase.
- [x] Subir los cambios a GitHub por solicitud del propietario; consolidar entrega en main.

## Revisión adicional del proyecto existente
- [x] Corregir permisos de `public.rls_auto_enable()`; Supabase Advisors de seguridad ya no informa avisos.

## Ampliación y entrega final
- [x] Ejecutar 20 pruebas unitarias Android y 8 instrumentadas (incluido cifrado con Keystore), cero fallos.
- [x] Ejecutar 17 pruebas Deno y comprobación de tipos de ambas funciones, cero fallos.
- [x] Validar restricciones SQL: minuto, día, global, pausa, eliminación y permisos.
- [x] Verificar firma RSA 3072 del APK release; mantener copia privada fuera de Git.
- [x] Preparar flujo de GitHub Actions para compilar, analizar y ejecutar pruebas.
- [x] Verificar GitHub Actions en remoto y publicar política de privacidad (HTTP 200).

El nuevo APK firmado 1.1 requiere completar la transición segura descrita en `docs/CONEXION_REAL.md`; no se declara validado en vivo mientras Anonymous Sign-Ins siga deshabilitado. El APK remoto del laboratorio entregado antes continúa disponible por separado.

- [x] Preparar informe Word en español con doce capturas de interfaces ejecutadas y sus explicaciones.
