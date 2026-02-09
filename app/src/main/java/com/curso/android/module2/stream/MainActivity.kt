package com.curso.android.module2.stream

/*Imports*/
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star // AGREGADO: Icono Filled para Highlights
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star // AGREGADO: Icono Outlined para Highlights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.curso.android.module2.stream.data.repository.MusicRepository
import com.curso.android.module2.stream.ui.navigation.HighlightsDestination // CAMBIO: Highlights
import com.curso.android.module2.stream.ui.navigation.HomeDestination
import com.curso.android.module2.stream.ui.navigation.PlayerDestination
import com.curso.android.module2.stream.ui.navigation.SearchDestination
import com.curso.android.module2.stream.ui.screens.HighlightsScreen // CAMBIO: Highlights Screen
import com.curso.android.module2.stream.ui.screens.HomeScreen
import com.curso.android.module2.stream.ui.screens.PlayerScreen
import com.curso.android.module2.stream.ui.screens.SearchScreen
import com.curso.android.module2.stream.ui.theme.StreamUITheme
import org.koin.compose.koinInject
import kotlin.reflect.KClass

/**
 * ================================================================================
 * CONCEPTOS TEÓRICOS Y ARQUITECTURA GENERAL
 * ================================================================================
 *
 * 1. MAIN ACTIVITY & SINGLE ACTIVITY ARCHITECTURE
 * -----------------------------------------------
 * En apps Compose modernas, típicamente usamos UNA sola Activity.
 * Toda la navegación se maneja internamente con Navigation Compose.
 *
 * Ventajas:
 * - Navegación más fluida (sin recrear Activities)
 * - Estado compartido más fácil
 * - Transiciones personalizables
 * - Mejor integración con Compose
 *
 * Componentes Clave:
 * - ComponentActivity: Base moderna para Compose
 * - setContent { }: Establece la raíz del árbol de Compose
 * - NavHost: Contenedor de destinos de navegación
 * - NavController: Controla la navegación (back stack)
 * - NavigationBar: Barra de navegación inferior (Bottom Navigation)
 *
 * 2. EDGE TO EDGE
 * ---------------
 * enableEdgeToEdge() hace que la app dibuje detrás de las barras
 * del sistema (status bar, navigation bar). Esto permite UIs
 * más inmersivas con colores personalizados en las barras.
 *
 * 3. BOTTOM NAVIGATION ARCHITECTURE
 * ---------------------------------
 * ESTRUCTURA:
 * La app tiene 3 tabs principales (Home, Search, Highlights) accesibles
 * desde el BottomNavigation. El Player es una pantalla de detalle
 * que se abre sobre cualquier tab.
 *
 * ```
 * ┌─────────────────────────────────┐
 * │          TopAppBar              │
 * ├─────────────────────────────────┤
 * │                                 │
 * │     Content (Home/Search/       │
 * │     Highlights/Player)          │
 * │                                 │
 * ├─────────────────────────────────┤
 * │  Home  │  Search  │ Highlights  │  ← BottomNavigation
 * └─────────────────────────────────┘
 * ```
 *
 * NAVEGACIÓN ENTRE TABS:
 * Usamos navigate() con opciones especiales para tabs:
 * - popUpTo(findStartDestination): Evita acumular back stack
 * - saveState/restoreState: Preserva el estado de cada tab
 * - launchSingleTop: Evita múltiples instancias del mismo destino
 *
 * 4. PATRÓN BOTTOM NAV ITEM
 * -------------------------
 * Cada item tiene:
 * - route: La clase de destino para navegación type-safe
 * - label: Texto que se muestra debajo del ícono
 * - selectedIcon: Ícono cuando el tab está seleccionado (filled)
 * - unselectedIcon: Ícono cuando el tab no está seleccionado (outlined)
 *
 * ICONOS FILLED vs OUTLINED:
 * Es una convención de Material Design usar iconos filled para
 * el estado seleccionado y outlined para el no seleccionado.
 * Esto proporciona feedback visual claro al usuario.
 *
 * 5. TYPE-SAFE NAVIGATION (Navigation 2.8+)
 * -----------------------------------------
 * En lugar de strings para las rutas, usamos tipos:
 * - composable<HomeDestination> { } en lugar de composable("home") { }
 * - navController.navigate(PlayerDestination(id)) en lugar de navigate("player/$id")
 */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita dibujo edge-to-edge (detrás de barras del sistema)
        enableEdgeToEdge()

        /**
         * setContent { }
         * Establece el contenido de la Activity usando Compose.
         * Todo lo que está dentro es un árbol de Composables.
         * Este es el ÚNICO lugar donde conectamos el mundo tradicional de Android (Activities) con el mundo de Compose.
         */
        setContent {
            StreamUITheme {
                StreamUIApp()
            }
        }
    }
}

/**
 * Define los elementos del BottomNavigation con sus propiedades.
 */
data class BottomNavItem(
    val route: KClass<*>,
    val label: String,
    val selectedIcon: @Composable () -> ImageVector,
    val unselectedIcon: @Composable () -> ImageVector
)

/**
 * Lista de items del BottomNavigation.
 *
 * CAMBIO PARTE 2:
 * Se reemplazó "Library" por "Highlights" usando íconos de estrella (Star).
 */
@Composable
fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem(
            route = HomeDestination::class,
            label = "Home",
            selectedIcon = { Icons.Filled.Home },
            unselectedIcon = { Icons.Outlined.Home }
        ),
        BottomNavItem(
            route = SearchDestination::class,
            label = "Search",
            selectedIcon = { Icons.Filled.Search },
            unselectedIcon = { Icons.Outlined.Search }
        ),
        // CAMBIO: Highlights Tab
        BottomNavItem(
            route = HighlightsDestination::class,
            label = "Highlights",
            selectedIcon = { Icons.Filled.Star },
            unselectedIcon = { Icons.Outlined.Star }
        )
    )
}

/**
 * Composable raíz de la aplicación.
 *
 * Configura:
 * 1. Surface con el color de fondo del tema
 * 2. NavController para manejar navegación
 * 3. Scaffold con TopAppBar y BottomNavigation
 * 4. NavHost con los destinos de la app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamUIApp() {
    /**
     * rememberNavController()
     * Crea y recuerda un NavController.
     * "Remember" significa que sobrevive recomposiciones.
     * El NavController mantiene el back stack de navegación.
     */
    val navController = rememberNavController()

    /**
     * koinInject()
     * Obtiene una dependencia del contenedor de Koin.
     * Aquí inyectamos el repository para buscar canciones por ID.
     */
    val repository: MusicRepository = koinInject()

    /**
     * currentBackStackEntryAsState()
     * Observa el estado actual del back stack como State.
     * Se recompone automáticamente cuando cambia el destino.
     *
     * Lo usamos para determinar qué tab está seleccionado
     * y si debemos mostrar el BottomNavigation.
     */
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    /**
     * Determinar si mostrar el BottomNavigation
     * El BottomNavigation solo se muestra en los tabs principales.
     * Se oculta en pantallas de detalle como Player.
     */
    val bottomNavItems = getBottomNavItems()
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route) == true
    }

    /**
     * Título dinámico del TopAppBar
     * Cambia según la pantalla actual para dar contexto al usuario.
     */
    val topBarTitle = when {
        currentDestination?.hasRoute(HomeDestination::class) == true -> "StreamUI"
        currentDestination?.hasRoute(SearchDestination::class) == true -> "Search"
        currentDestination?.hasRoute(HighlightsDestination::class) == true -> "Highlights" // CAMBIO: Título actualizado
        currentDestination?.hasRoute(PlayerDestination::class) == true -> "Now Playing"
        else -> "StreamUI"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            /**
             * TOP APP BAR
             * Barra superior con el título de la pantalla actual.
             */
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            /**
             * BOTTOM NAVIGATION BAR
             * NavigationBar es el componente Material 3 para bottom navigation.
             *
             * Solo se muestra en los tabs principales (Home, Search, Highlights).
             */
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            /**
                             * ESTADO SELECCIONADO
                             * Usamos hierarchy para verificar si el destino actual
                             * está en la jerarquía del item. Esto maneja correctamente
                             * el caso de destinos anidados.
                             */
                            val selected = currentDestination?.hierarchy?.any {
                                it.hasRoute(item.route)
                            } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    /**
                                     * NAVEGACIÓN DE TABS
                                     * La navegación entre tabs requiere opciones especiales.
                                     */
                                    navController.navigate(
                                        when (item.route) {
                                            HomeDestination::class -> HomeDestination
                                            SearchDestination::class -> SearchDestination
                                            HighlightsDestination::class -> HighlightsDestination // CAMBIO: Ruta actualizada
                                            else -> HomeDestination
                                        }
                                    ) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon() else item.unselectedIcon(),
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            /**
             * NAVHOST: Contenedor de Navegación
             * NavHost define el grafo de navegación de la app.
             *
             * Parámetros:
             * - navController: Controla la navegación
             * - startDestination: Destino inicial (HomeDestination)
             */
            NavHost(
                navController = navController,
                startDestination = HomeDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                /**
                 * DESTINO: Home Screen
                 */
                composable<HomeDestination> {
                    HomeScreen(
                        onSongClick = { song ->
                            navController.navigate(PlayerDestination(songId = song.id))
                        }
                    )
                }

                /**
                 * DESTINO: Search Screen
                 */
                composable<SearchDestination> {
                    SearchScreen(
                        onSongClick = { song ->
                            navController.navigate(PlayerDestination(songId = song.id))
                        },
                        onBackClick = {
                            // En BottomNavigation, Search es un tab principal
                        }
                    )
                }

                /**
                 * DESTINO: Highlights Screen (Favoritos)
                 * --------------------------------------
                 * Reemplaza a LibraryScreen.
                 * Muestra solo las canciones marcadas con corazón.
                 */
                composable<HighlightsDestination> {
                    HighlightsScreen(
                        onSongClick = { songId ->
                            navController.navigate(PlayerDestination(songId = songId))
                        }
                    )
                }

                /**
                 * DESTINO: Player Screen
                 */
                composable<PlayerDestination> { backStackEntry ->
                    // Extrae los argumentos de navegación de forma type-safe
                    val destination = backStackEntry.toRoute<PlayerDestination>()

                    // Busca la canción en el repository
                    val song = repository.getSongById(destination.songId)

                    PlayerScreen(
                        song = song,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}