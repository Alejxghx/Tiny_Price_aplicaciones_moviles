# Privacidad de TinyPrice

Actualizada: 23 de octubre de 2026. Versión 1.1, proyecto educativo de Alejxghx.

## Datos utilizados

TinyPrice envía el texto de la búsqueda a Supabase y a TinyFish para encontrar páginas de productos. Las tiendas reciben una visita solo cuando abres su enlace en el navegador, donde rigen sus políticas. No introduzcas datos sensibles en la búsqueda.

La modalidad remota utiliza Supabase Auth para crear una sesión de invitado sin correo ni contraseña. Sus identificadores y contadores de uso permiten aplicar límites. Los tokens se guardan cifrados mediante Android Keystore en almacenamiento local excluido de las copias de seguridad. La app no incorpora anuncios ni SDK de analítica.

La base de datos de TinyPrice no guarda el texto de las búsquedas ni un historial de productos. Guarda identificadores de sesión y contadores por minuto/día. El servidor elimina contadores de más de siete días al recibir nuevas solicitudes; si no hay tráfico, pueden permanecer hasta la siguiente limpieza. Supabase y TinyFish procesan solicitudes y pueden conservar registros técnicos conforme a sus propias condiciones. No se ha configurado aquí un plazo contractual de retención de esos proveedores.

## Eliminar datos

En la app abre **Privacidad y mis datos → Eliminar mis datos → Eliminar datos**. Se elimina la cuenta de invitado en Supabase, sus contadores individuales y sus tokens locales. El contador global agregado no identifica a una persona y se conserva temporalmente para limitar el gasto. Se necesita conexión; si la operación falla, la app conserva la sesión para reintentar. Una búsqueda posterior crea una sesión nueva.

También puedes solicitar ayuda en [Issues del proyecto](https://github.com/Alejxghx/Tiny_Price_aplicaciones_moviles/issues). No publiques contraseñas, claves ni tokens. Esta es también la vía externa para solicitar asistencia con la eliminación; el propietario deberá coordinar una verificación privada antes de acceder a datos de una sesión.

Desinstalar borra los tokens locales, pero no garantiza eliminar inmediatamente la cuenta remota. Usa la opción de eliminación antes de desinstalar.

## Seguridad y consultas

Las conexiones remotas utilizan HTTPS. La clave de TinyFish queda en el servidor. Los límites reducen el uso indebido, pero una sesión de invitado puede recrearse: no equivalen a una identidad verificada. El proyecto es educativo y los resultados requieren comprobar precio y disponibilidad en la fuente.

Responsable del proyecto: [Alejxghx](https://github.com/Alejxghx). Canal de contacto: [Issues de TinyPrice](https://github.com/Alejxghx/Tiny_Price_aplicaciones_moviles/issues). El propietario debe revisar esta política y los requisitos aplicables antes de una publicación comercial.
