# 📱 Android Studio Module 2: Advanced State & Navigation

Este repositorio contiene el proyecto práctico **Stream**, desarrollado con **Kotlin** y **Jetpack Compose**. El objetivo principal es demostrar el dominio de la arquitectura de navegación compleja, el paso de argumentos tipados y la gestión de estado compartido entre múltiples pantallas.


## Screenshots

<p align="center">
  <img src="assets/screenshot_1.png" width="30%" />
  <img src="assets/screenshot_2.png" width="30%" />
  <img src="assets/screenshot_3.png" width="30%" />
</p>

## Tech Stack & Conceptos Clave

* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3)
* **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Navegación:** Navigation Compose 2.8+ (Type-Safe con `@Serializable`)
* **Gestión de Estado:** `StateFlow`, `collectAsState`, `State Hoisting`.
* **Listas Eficientes:** `LazyColumn`, `LazyRow`.
* **Persistencia de Datos (Mock):** Data Classes con estado mutable (`isFavorite`).

## 🎵 Proyecto: Stream Music App

Una aplicación de reproducción de música moderna que implementa un sistema de navegación completo y sincronización de datos en tiempo real entre pantallas.

### Características Principales

* **Navegación Inferior (Bottom Navigation):** Implementación de un `Scaffold` con `NavigationBar` para transitar entre las secciones *Home*, *Search* y *Highlights*.
* **Sistema de Favoritos (Highlights):**
    * Funcionalidad de "Me gusta" (❤️) interactiva en cada tarjeta de canción.
    * **Sincronización en tiempo real:** Al marcar una canción en el *Home*, aparece instantáneamente en la pestaña *Highlights*.
    * Filtrado dinámico de listas basado en el estado del modelo.
* **Navegación Type-Safe:** Paso de argumentos complejos (IDs de canciones) hacia la pantalla de *Player* utilizando objetos serializables en lugar de strings propensos a errores.
* **Componentes Reutilizables:** Diseño modular con `SongCard` y `SongCoverMock` que se adaptan a diferentes contextos (listas horizontales o verticales).

### Implementación Técnica

* **Single Source of Truth:** Se utiliza un `Repository` centralizado. Las pantallas no guardan datos, solo observan los cambios. Esto permite que el estado de "Favorito" se comparta globalmente.
* **Event Hoisting:** El componente `SongCard` es *stateless* (sin estado). No decide cuándo cambiar el ícono; en su lugar, propaga el evento `onFavoriteClick` hacia el `ViewModel`, que actualiza el modelo de datos.
* **Type-Safe Navigation:** Uso de `kotlinx.serialization` para definir rutas como objetos (`HighlightsDestination`, `PlayerDestination`) garantizando seguridad de tipos en tiempo de compilación.
* **Reactive UI:** La interfaz reacciona automáticamente a los cambios en el `isFavorite` del modelo de datos `Song`.

## 📸 Cómo probar el proyecto

1.  **Clonar el repositorio** en tu máquina local.
2.  Abrir **Android Studio**.
3.  Selecciona **File > Open** y elige la carpeta raíz del proyecto `Stream`.
4.  Espera a que Gradle sincronice las dependencias.
5.  Ejecuta el módulo **app** con el botón de Play ▶️ en un emulador (API 26 o superior).

### Verificación de Funcionalidad
1.  En la pantalla **Home**, toca el corazón de cualquier canción.
2.  Navega a la pestaña **Highlights** (ícono de estrella ⭐).
3.  Verifica que la canción seleccionada aparece allí.
4.  Desmarca la canción en Highlights y comprueba que se actualiza en el Home.

Link al video explicativo:
https://youtube.com/shorts/s3fPAc2XPWA?feature=share
