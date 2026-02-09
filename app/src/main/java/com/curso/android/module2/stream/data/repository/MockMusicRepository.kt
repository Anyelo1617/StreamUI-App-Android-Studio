package com.curso.android.module2.stream.data.repository

import com.curso.android.module2.stream.data.model.Category
import com.curso.android.module2.stream.data.model.Playlist
import com.curso.android.module2.stream.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * ================================================================================
 * MOCK MUSIC REPOSITORY
 * ================================================================================
 *
 * Implementación del repositorio que proporciona datos simulados.
 *
 * PATRÓN REPOSITORY
 * -----------------
 * El patrón Repository abstrae el origen de los datos de la lógica de negocio.
 * La UI y los ViewModels no saben (ni les importa) de dónde vienen los datos:
 * - Base de datos local (Room)
 * - API remota (Retrofit)
 * - Datos mock (este caso)
 *
 * Beneficios:
 * 1. Fácil testing: Inyecta un mock repository en tests
 * 2. Cambio de fuente de datos sin modificar ViewModels
 * 3. Single source of truth para los datos
 *
 * IMPLEMENTANDO UNA INTERFACE
 * ---------------------------
 * Esta clase implementa MusicRepository, permitiendo que los ViewModels
 * dependan de la interface (abstracción) en lugar de esta clase concreta.
 *
 * En producción podrías tener:
 * - MockMusicRepository: Para desarrollo y previews (este archivo)
 * - RemoteMusicRepository: Para producción con API real
 * - CachedMusicRepository: Con cache local + sincronización remota
 *
 * La inyección de dependencias (Koin) decide cuál usar en cada contexto.
 */
class MockMusicRepository : MusicRepository {

    // SINGLE SOURCE OF TRUTH (Mutable)
    // Inicializamos el flujo con los datos estáticos del companion object
    private val _categoriesFlow = MutableStateFlow(initialCategories)

    /**
     * Obtiene todas las categorías con sus canciones.
     * Retorna el valor actual del flujo.
     */
    override fun getCategories(): List<Category> = _categoriesFlow.value

    /**
     * Busca una canción por su ID.
     *
     * @param songId ID de la canción a buscar
     * @return La canción encontrada o null si no existe
     *
     * Nota: flatMap aplana la lista de listas de canciones en una sola lista
     */
    override fun getSongById(songId: String): Song? {
        return _categoriesFlow.value
            .flatMap { it.songs }
            .find { it.id == songId }
    }

    /**
     * Obtiene todas las canciones de todas las categorías.
     *
     * @return Lista plana de todas las canciones disponibles
     *
     * Útil para búsquedas globales donde no importa la categoría.
     * flatMap convierte List<Category> → List<Song> aplanando las listas anidadas.
     */
    override fun getAllSongs(): List<Song> {
        return _categoriesFlow.value.flatMap { it.songs }
    }

    /**
     * Obtiene las playlists del usuario.
     *
     * CAMBIO IMPORTANTE:
     * Ahora es DINÁMICO. La playlist "My Favorites" se reconstruye cada vez
     * que se llama a este método para reflejar los likes actuales.
     */
    override fun getPlaylists(): List<Playlist> {
        // 1. Obtenemos todas las canciones actuales (con sus estados de like actualizados)
        val allSongs = getAllSongs()

        // 2. Filtramos las que tienen like
        val favoriteSongs = allSongs.filter { it.isFavorite }

        // 3. Creamos la playlist de favoritos dinámica
        val myFavorites = Playlist(
            id = "playlist_favorites",
            name = "My Favorites",
            description = "Songs I love the most",
            songCount = favoriteSongs.size, // ¡El contador ahora es real!
            colorSeed = 0xFF1DB954.toInt(), // Verde tipo Spotify
            songs = favoriteSongs // Asignamos las canciones
        )

        // 4. Retornamos Favoritos + Las otras playlists estáticas
        return listOf(myFavorites) + staticPlaylists
    }

    /**
     * Obtenemos todas las canciones de todas las categorías como un Flow reactivo.
     * Cuando cambia un favorito, este Flow emite la nueva lista automáticamente.
     */
    override fun getSongs(): Flow<List<Song>> {
        return _categoriesFlow.map { currentCategories ->
            currentCategories.flatMap { it.songs }
        }
    }

    /**
     * Lógica "Deep Copy" para actualizar un favorito dentro de una estructura anidada.
     * Al actualizar el _categoriesFlow, todas las pantallas que observan se actualizan.
     */
    override fun toggleFavorite(songId: String) {
        _categoriesFlow.update { currentCategories ->
            currentCategories.map { category ->
                // Verificamos si la canción está en esta categoría
                val containsSong = category.songs.any { it.id == songId }

                if (containsSong) {
                    // Reconstruimos la categoría con la canción actualizada
                    category.copy(
                        songs = category.songs.map { song ->
                            if (song.id == songId) {
                                song.copy(isFavorite = !song.isFavorite)
                            } else {
                                song
                            }
                        }
                    )
                } else {
                    category // Si no está aquí, devolvemos la categoría tal cual
                }
            }
        }
    }

    companion object {
        /**
         * Datos mock de la aplicación.
         *
         * colorSeed: Valores diferentes generan gradientes únicos.
         * Usamos valores espaciados para maximizar la variedad visual.
         */
        private val initialCategories = listOf(
            // ==========================================
            // CATEGORÍA 1: Rock Classics
            // ==========================================
            Category(
                name = "Rock Classics",
                songs = listOf(
                    Song("rock_1", "Highway to Hell", "AC/DC", 0xFF1E88E5.toInt(), "", false),
                    Song("rock_2", "Bohemian Rhapsody", "Queen", 0xFF8E24AA.toInt(), "", false),
                    Song("rock_3", "Stairway to Heaven", "Led Zeppelin", 0xFF43A047.toInt(), "", false),
                    Song("rock_4", "Sweet Child O' Mine", "Guns N' Roses", 0xFFE53935.toInt(), "", false),
                    Song("rock_5", "Back in Black", "AC/DC", 0xFF3949AB.toInt(), "", false),
                    Song("rock_6", "Hotel California", "Eagles", 0xFFFB8C00.toInt(), "", false),
                    Song("rock_7", "Comfortably Numb", "Pink Floyd", 0xFF00ACC1.toInt(), "", false),
                    Song("rock_8", "November Rain", "Guns N' Roses", 0xFF7CB342.toInt(), "", false),
                    Song("rock_9", "Dream On", "Aerosmith", 0xFFD81B60.toInt(), "", false),
                    Song("rock_10", "Smoke on the Water", "Deep Purple", 0xFF5E35B1.toInt(), "", false)
                )
            ),

            // ==========================================
            // CATEGORÍA 2: Coding Focus
            // ==========================================
            Category(
                name = "Coding Focus",
                songs = listOf(
                    Song("code_1", "Weightless", "Marconi Union", 0xFF26A69A.toInt(), "", false),
                    Song("code_2", "Clair de Lune", "Debussy", 0xFF5C6BC0.toInt(), "", false),
                    Song("code_3", "Experience", "Ludovico Einaudi", 0xFF66BB6A.toInt(), "", false),
                    Song("code_4", "Time", "Hans Zimmer", 0xFF42A5F5.toInt(), "", false),
                    Song("code_5", "Gymnopédie No.1", "Erik Satie", 0xFFAB47BC.toInt(), "", false),
                    Song("code_6", "River Flows in You", "Yiruma", 0xFF26C6DA.toInt(), "", false),
                    Song("code_7", "Nuvole Bianche", "Ludovico Einaudi", 0xFF9CCC65.toInt(), "", false),
                    Song("code_8", "Interstellar Main Theme", "Hans Zimmer", 0xFF7E57C2.toInt(), "", false),
                    Song("code_9", "Arrival of the Birds", "The Cinematic Orchestra", 0xFFFFCA28.toInt(), "", false),
                    Song("code_10", "On the Nature of Daylight", "Max Richter", 0xFFEF5350.toInt(), "", false)
                )
            ),

            // ==========================================
            // CATEGORÍA 3: Gym Energy
            // ==========================================
            Category(
                name = "Gym Energy",
                songs = listOf(
                    Song("gym_1", "Stronger", "Kanye West", 0xFFFF7043.toInt(), "", false),
                    Song("gym_2", "Lose Yourself", "Eminem", 0xFF78909C.toInt(), "", false),
                    Song("gym_3", "Eye of the Tiger", "Survivor", 0xFFFFA726.toInt(), "", false),
                    Song("gym_4", "Till I Collapse", "Eminem", 0xFF5C6BC0.toInt(), "", false),
                    Song("gym_5", "Can't Hold Us", "Macklemore", 0xFF66BB6A.toInt(), "", false),
                    Song("gym_6", "Power", "Kanye West", 0xFFEC407A.toInt(), "", false),
                    Song("gym_7", "Remember the Name", "Fort Minor", 0xFF29B6F6.toInt(), "", false),
                    Song("gym_8", "Thunderstruck", "AC/DC", 0xFFFFEE58.toInt(), "", false),
                    Song("gym_9", "Believer", "Imagine Dragons", 0xFFAB47BC.toInt(), "", false),
                    Song("gym_10", "Warriors", "Imagine Dragons", 0xFF26A69A.toInt(), "", false)
                )
            ),

            // ==========================================
            // CATEGORÍA 4: Chill Vibes
            // ==========================================
            Category(
                name = "Chill Vibes",
                songs = listOf(
                    Song("chill_1", "Sunset Lover", "Petit Biscuit", 0xFFFFAB91.toInt(), "", false),
                    Song("chill_2", "Intro", "The xx", 0xFF90A4AE.toInt(), "", false),
                    Song("chill_3", "Midnight City", "M83", 0xFFCE93D8.toInt(), "", false),
                    Song("chill_4", "Electric Feel", "MGMT", 0xFF80DEEA.toInt(), "", false),
                    Song("chill_5", "Breathe", "Télépopmusik", 0xFFA5D6A7.toInt(), "", false),
                    Song("chill_6", "Teardrop", "Massive Attack", 0xFFB39DDB.toInt(), "", false),
                    Song("chill_7", "Porcelain", "Moby", 0xFF81D4FA.toInt(), "", false),
                    Song("chill_8", "Fade Into You", "Mazzy Star", 0xFFF48FB1.toInt(), "", false),
                    Song("chill_9", "Skinny Love", "Bon Iver", 0xFFFFCC80.toInt(), "", false),
                    Song("chill_10", "Holocene", "Bon Iver", 0xFFC5E1A5.toInt(), "", false)
                )
            ),

            // ==========================================
            // CATEGORÍA 5: Latin Hits
            // ==========================================
            Category(
                name = "Latin Hits",
                songs = listOf(
                    Song("latin_1", "Despacito", "Luis Fonsi", 0xFFFF8A65.toInt(), "", false),
                    Song("latin_2", "Bailando", "Enrique Iglesias", 0xFFFFD54F.toInt(), "", false),
                    Song("latin_3", "La Bicicleta", "Shakira & Carlos Vives", 0xFF4DD0E1.toInt(), "", false),
                    Song("latin_4", "Vivir Mi Vida", "Marc Anthony", 0xFFAED581.toInt(), "", false),
                    Song("latin_5", "Danza Kuduro", "Don Omar", 0xFFBA68C8.toInt(), "", false),
                    Song("latin_6", "Livin' la Vida Loca", "Ricky Martin", 0xFFFF8A80.toInt(), "", false),
                    Song("latin_7", "Waka Waka", "Shakira", 0xFF82B1FF.toInt(), "", false),
                    Song("latin_8", "Súbeme la Radio", "Enrique Iglesias", 0xFFB9F6CA.toInt(), "", false),
                    Song("latin_9", "Mi Gente", "J Balvin", 0xFFFFE57F.toInt(), "", false),
                    Song("latin_10", "Gasolina", "Daddy Yankee", 0xFFEA80FC.toInt(), "", false)
                )
            )
        )

        /**
         * Playlists estáticas (excepto Favoritos que se genera dinámicamente).
         */
        private val staticPlaylists = listOf(
            Playlist(
                id = "playlist_2",
                name = "Workout Mix",
                description = "High energy tracks for the gym",
                songCount = 18,
                colorSeed = 0xFFE53935.toInt()
            ),
            Playlist(
                id = "playlist_3",
                name = "Chill Evening",
                description = "Relaxing tunes for unwinding",
                songCount = 32,
                colorSeed = 0xFF7CB342.toInt()
            ),
            Playlist(
                id = "playlist_4",
                name = "Road Trip",
                description = "Perfect for long drives",
                songCount = 45,
                colorSeed = 0xFFFB8C00.toInt()
            ),
            Playlist(
                id = "playlist_5",
                name = "Focus Mode",
                description = "Concentration and productivity",
                songCount = 20,
                colorSeed = 0xFF8E24AA.toInt()
            ),
            Playlist(
                id = "playlist_6",
                name = "Party Hits",
                description = "Get the party started",
                songCount = 38,
                colorSeed = 0xFF00ACC1.toInt()
            )
        )
    }
}