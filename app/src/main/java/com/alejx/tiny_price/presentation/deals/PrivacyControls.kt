package com.alejx.tiny_price.presentation.deals

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.alejx.tiny_price.data.session.SessionRegistry
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@Composable
fun PrivacyControls(onDeleted: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    TextButton(onClick = { visible = true }) { Text("Privacidad y mis datos") }
    if (visible) AlertDialog(
        onDismissRequest = { if (!busy) { visible = false; confirmDelete = false } },
        title = { Text(if (confirmDelete) "¿Eliminar mis datos?" else "Privacidad") },
        text = { Text(error ?: if (busy) "Eliminando datos…" else if (confirmDelete)
            "Se eliminará tu sesión de invitado y sus contadores de uso. Después podrás comenzar una sesión nueva."
            else "Las búsquedas se envían a Supabase y TinyFish para encontrar fuentes web. No guardamos el texto de tus búsquedas en la base de datos. Usamos una sesión de invitado y contadores temporales para limitar el uso.") },
        confirmButton = {
            TextButton(enabled = !busy, onClick = {
                if (!confirmDelete) { confirmDelete = true; error = null }
                else scope.launch {
                    busy = true
                    try {
                        SessionRegistry.get().deleteAccount()
                        onDeleted()
                        visible = false; confirmDelete = false; error = null
                        Toast.makeText(context, "Datos de búsqueda eliminados.", Toast.LENGTH_LONG).show()
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { error = "No se pudieron eliminar los datos. Revisa tu conexión y reintenta." }
                    finally { busy = false }
                }
            }) { Text(if (confirmDelete) "Eliminar datos" else "Eliminar mis datos") }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = {
                if (confirmDelete) { confirmDelete = false; error = null }
                else {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://alejxghx.github.io/Tiny_Price_aplicaciones_moviles/"))) }
                    catch (_: ActivityNotFoundException) { error = "Abre la política desde el README del proyecto." }
                }
            }) { Text(if (confirmDelete) "Cancelar" else "Ver política completa") }
        },
    )
}
