package com.example.safemotion.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Guarda las fotos de evidencia en el almacenamiento interno de la app
 */
class EvidencePhotoStorage(context: Context) {
    private val appContext = context.applicationContext

    private val folder: File
        get() = File(appContext.filesDir, "evidence").apply { mkdirs() }

    // Archivo vacío donde la cámara escribirá la foto.
    fun newPhotoFile(): File = File(folder, "evidencia_${System.currentTimeMillis()}.jpg")

    // Uri segura para que la app de cámara pueda escribir en nuestro archivo interno.
    fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)

    // Copia una foto elegida en la galería a nuestro almacenamiento interno.
    suspend fun copyFromGallery(source: Uri): File? = withContext(Dispatchers.IO) {
        val target = newPhotoFile()
        try {
            val input = appContext.contentResolver.openInputStream(source) ?: return@withContext null
            input.use { stream ->
                target.outputStream().use { output -> stream.copyTo(output) }
            }
            target
        } catch (e: IOException) {
            target.delete()
            null
        }
    }

    fun delete(path: String) {
        File(path).delete()
    }
}