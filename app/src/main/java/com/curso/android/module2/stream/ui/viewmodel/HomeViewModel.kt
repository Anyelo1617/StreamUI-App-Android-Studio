package com.curso.android.module2.stream.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module2.stream.data.model.Category
import com.curso.android.module2.stream.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ================================================================================
 * HOME VIEW MODEL - Lógica de Presentación
 * ================================================================================
 *
 * PATRÓN MVVM (Model-View-ViewModel)
 * ----------------------------------
 * MVVM separa la aplicación en tres capas:
 *
 * 1. MODEL (data/):
 * - Datos y lógica de negocio
 * - Repository, Data Sources, Entities
 * - No conoce la UI
 *
 * 2. VIEW (ui/screens/):
 * - Composables que renderizan la UI
 * - Observa el estado del ViewModel
 * - Envía eventos de usuario al ViewModel
 * - NO contiene lógica de negocio
 *
 * 3. VIEWMODEL (ui/viewmodel/):
 * - Puente entre Model y View
 * - Expone estado observable (StateFlow)
 * - Procesa eventos de la UI
 * - Sobrevive cambios de configuración (rotación)
 *
 * FLUJO DE DATOS (UDF - Unidirectional Data Flow):
 * ------------------------------------------------
 *
 * ┌─────────────────────────────────────────────┐
 * │                                             │
 * │    ┌──────────┐    State    ┌──────────┐   │
 * │    │ViewModel │ ──────────▶ │   View   │   │
 * │    └──────────┘             └──────────┘   │
 * │         ▲                        │         │
 * │         │       Events           │         │
 * │         └────────────────────────┘         │
 * │                                             │
 * └─────────────────────────────────────────────┘
 *
 * - STATE fluye del ViewModel a la View (UI observa StateFlow)
 * - EVENTS fluyen de la View al ViewModel (clicks, inputs, etc.)
 * - NUNCA al revés: la View no modifica el estado directamente
 *
 * BENEFICIOS DE UDF:
 * 1. Predecibilidad: El estado solo cambia desde el ViewModel
 * 2. Debugging: Fácil rastrear cambios de estado
 * 3. Testing: Puedes verificar estados sin UI
 * 4. Compose: Se integra perfectamente con recomposición
 */

/**
 * Estado de la pantalla Home.
 *
 * Sealed interface permite representar múltiples estados posibles
 * de forma type-safe. Cada estado es una clase/objeto específico.
 *
 * ESTADOS TÍPICOS:
 * - Loading: Cargando datos
 * - Success: Datos cargados correctamente
 * - Error: Ocurrió un error
 *
 * En este ejemplo simplificado solo usamos Success ya que
 * los datos son síncronos (mock). En una app real tendrías
 * todos los estados.
 */
sealed interface HomeUiState {
    /**
     * Estado inicial mientras se cargan los datos.
     */
    data object Loading : HomeUiState

    /**
     * Datos cargados exitosamente.
     *
     * @property categories Lista de categorías con sus canciones
     */
    data class Success(
        val categories: List<Category>
    ) : HomeUiState

    /**
     * Error al cargar los datos.
     *
     * @property message Mensaje descriptivo del error
     */
    data class Error(
        val message: String
    ) : HomeUiState
}

/**
 * ViewModel para la pantalla Home.
 *
 * @param repository Repositorio de música (inyectado por Koin)
 *
 * INYECCIÓN DE DEPENDENCIAS CON INTERFACE:
 * ----------------------------------------
 * El repository se pasa como parámetro del constructor, pero ahora
 * depende de la INTERFACE MusicRepository en lugar de la clase concreta.
 *
 * Esto permite:
 * - Testing: Inyectar un FakeMusicRepository en tests
 * - Flexibilidad: Cambiar la implementación sin modificar el ViewModel
 * - Principio de Inversión de Dependencias (DIP): El ViewModel depende
 * de una abstracción, no de una implementación concreta
 */
class HomeViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    /**
     * STATE HOLDER (MutableStateFlow)
     * --------------------------------
     * MutableStateFlow es un holder de estado observable:
     * - Siempre tiene un valor (no nullable)
     * - Emite el valor actual a nuevos collectors
     * - Solo emite cuando el valor CAMBIA (distinctUntilChanged)
     *
     * Es PRIVADO porque solo el ViewModel debe modificar el estado.
     * La UI solo puede LEER a través de uiState (inmutable).
     */
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /**
     * EXPOSED STATE (StateFlow)
     * --------------------------
     * Versión inmutable del estado, expuesta a la UI.
     *
     * asStateFlow() convierte MutableStateFlow a StateFlow (solo lectura).
     *
     * En Compose, la UI observa este StateFlow con collectAsState():
     * ```kotlin
     * val uiState by viewModel.uiState.collectAsState()
     * ```
     *
     * Cuando _uiState cambia, Compose automáticamente recompone
     * los composables que dependen de este valor.
     */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Inicialización del ViewModel.
     *
     * init {} se ejecuta cuando se crea el ViewModel.
     * Ahora llamamos a observeMusicData() para suscribirnos al flujo de datos.
     */
    init {
        observeMusicData()
    }

    /**
     * Observa los cambios en los datos del repositorio de forma reactiva.
     *
     * CAMBIO A REACTIVO (FLOW):
     * -------------------------
     * En lugar de pedir los datos una vez, nos "suscribimos" (collect) al flujo
     * que provee el repositorio.
     *
     * 1. `repository.getSongs()`: Es un Flow que emite cada vez que algo cambia (ej. un Like).
     * 2. `collectLatest`: Cada vez que el Flow emite, ejecutamos este bloque.
     * 3. Dentro del bloque, obtenemos la lista actualizada de categorías y actualizamos la UI.
     *
     * Esto asegura que la UI siempre esté sincronizada con la base de datos,
     * sin necesidad de recargar manualmente.
     */
    private fun observeMusicData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                // Suscripción al Flow del repositorio
                repository.getSongs().collectLatest {
                    // Nota: Aunque el flow emite canciones, usamos este evento como
                    // señal para recargar la estructura completa de categorías actualizada.
                    val categories = repository.getCategories()
                    _uiState.update { HomeUiState.Success(categories) }
                }
            } catch (e: Exception) {
                _uiState.update { HomeUiState.Error(e.message ?: "Unknown error") }
            }
        }
    }

    /**
     * Recarga los datos (reinicia la observación).
     *
     * En un entorno puramente reactivo con Flows, "refrescar" suele ser innecesario
     * porque los datos llegan solos, pero mantenemos el método por si se requiere
     * reiniciar el flujo en caso de error.
     */
    fun refresh() {
        observeMusicData()
    }

    /**
     * Cambia el estado de favorito de una canción.
     *
     * @param songId El ID de la canción a modificar.
     *
     * FLUJO DE INTERACCIÓN:
     * 1. UI llama a toggleFavorite(id)
     * 2. Repository actualiza la lista interna
     * 3. Repository emite nuevo valor en el Flow getSongs()
     * 4. observeMusicData() (arriba) detecta el cambio
     * 5. ViewModel emite nuevo UI State
     * 6. UI se recompone con el corazón actualizado
     */
    fun toggleFavorite(songId: String) {
        repository.toggleFavorite(songId)
    }
}