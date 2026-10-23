package com.example.safemotion.ui.screens.reportrisk

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.RiskCategory
import com.example.safemotion.data.repository.EvidencePhotoStorage
import com.example.safemotion.data.repository.LocationRepository
import com.example.safemotion.data.repository.MockRiskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class ReportRiskViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportRiskUiState())
    val uiState = _uiState.asStateFlow()

    // Archivo que la cámara está llenando mientras está abierta.
    private var pendingCameraPath: String? = null

    fun selectCategory(category: RiskCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun changeDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    // Se llama cuando el usuario ya dio el permiso de ubicación.
    fun useCurrentLocation(repository: LocationRepository) {
        if (_uiState.value.isLocating) return
        _uiState.update { it.copy(isLocating = true, locationUnavailable = false) }
        viewModelScope.launch {
            val point = repository.currentLocation()
            if (point == null) {
                _uiState.update { it.copy(isLocating = false, locationUnavailable = true) }
            } else {
                setLocation(point, repository.addressFor(point))
            }
        }
    }

    // Guarda las coordenadas; si el Geocoder no encontró dirección, se muestran las coordenadas.
    fun setLocation(point: MapPoint, address: String?) {
        val label = address ?: String.format(Locale.US, "%.5f, %.5f", point.latitude, point.longitude)
        _uiState.update {
            it.copy(
                location = label,
                latitude = point.latitude,
                longitude = point.longitude,
                isLocating = false,
                locationUnavailable = false
            )
        }
    }

    // Se llama si el usuario negó el permiso de ubicación.
    fun onLocationDenied() {
        _uiState.update { it.copy(isLocating = false, locationUnavailable = true) }
    }

    // Crea el archivo interno donde la cámara guardará la foto y devuelve su Uri segura.
    fun prepareCameraPhoto(storage: EvidencePhotoStorage): Uri {
        val file = storage.newPhotoFile()
        pendingCameraPath = file.absolutePath
        _uiState.update { it.copy(cameraDenied = false, photoFailed = false) }
        return storage.uriFor(file)
    }

    // La cámara terminó, si se tomó la foto, queda como evidencia, si se canceló se borra el archivo vacío.
    fun onCameraResult(success: Boolean, storage: EvidencePhotoStorage) {
        val path = pendingCameraPath ?: return
        pendingCameraPath = null
        if (success) {
            replacePhoto(path, PhotoSource.CAMERA, storage)
        } else {
            storage.delete(path)
        }
    }

    fun onCameraDenied() {
        _uiState.update { it.copy(cameraDenied = true) }
    }

    // Copia la foto elegida en la galería al almacenamiento interno.
    fun importFromGallery(uri: Uri, storage: EvidencePhotoStorage) {
        if (_uiState.value.isSavingPhoto) return
        _uiState.update { it.copy(isSavingPhoto = true, photoFailed = false) }
        viewModelScope.launch {
            val file = storage.copyFromGallery(uri)
            if (file != null) replacePhoto(file.absolutePath, PhotoSource.GALLERY, storage)
            _uiState.update { it.copy(isSavingPhoto = false, photoFailed = file == null) }
        }
    }

    fun attachPhoto(path: String, source: PhotoSource) {
        _uiState.update {
            it.copy(photoPath = path, photoSource = source, photoFailed = false, cameraDenied = false)
        }
    }

    fun removePhoto(storage: EvidencePhotoStorage? = null) {
        _uiState.value.photoPath?.let { storage?.delete(it) }
        _uiState.update { it.copy(photoPath = null, photoSource = null) }
    }

    fun submit(): Boolean {
        val state = _uiState.value
        val category = state.category
        if (state.sent || !state.isComplete || category == null) return false
        MockRiskRepository.addReport(
            category = category,
            description = state.description.trim(),
            place = state.location,
            latitude = state.latitude ?: MockRiskRepository.DEFAULT_LATITUDE,
            longitude = state.longitude ?: MockRiskRepository.DEFAULT_LONGITUDE,
            photoPath = state.photoPath
        )
        _uiState.update { it.copy(sent = true) }
        return true
    }

    // Si se cierra sin enviar, la foto ya no sirve y se borra del almacenamiento interno.
    fun reset(storage: EvidencePhotoStorage? = null) {
        val state = _uiState.value
        if (!state.sent) state.photoPath?.let { storage?.delete(it) }
        _uiState.value = ReportRiskUiState()
    }

    // Si el reporte anterior ya se envió, el formulario vuelve a empezar vacío.
    fun prepareNewReport() {
        if (_uiState.value.sent) reset()
    }

    // Al cambiar de foto, la anterior se borra para no dejar archivos huérfanos.
    private fun replacePhoto(path: String, source: PhotoSource, storage: EvidencePhotoStorage) {
        val previous = _uiState.value.photoPath
        if (previous != null && previous != path) storage.delete(previous)
        attachPhoto(path, source)
    }
}