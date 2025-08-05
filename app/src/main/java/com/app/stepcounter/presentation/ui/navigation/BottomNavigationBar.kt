package com.app.stepcounter.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

// La tua enum class è perfetta e non necessita di modifiche.
enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    Home("home", "Steps", Icons.AutoMirrored.Filled.DirectionsWalk),
    Parties("parties", "Party", Icons.Default.Group)
}

@Composable
fun BottomNavigationBar(
    // 1. Accetta la rotta corrente come String (può essere nulla all'inizio)
    currentRoute: String?,
    // 2. La funzione di callback ora restituisce una String (la rotta)
    onScreenSelected: (String) -> Unit
) {
    NavigationBar {
        // Itera su tutte le schermate definite nella tua enum
        Screen.entries.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                // 3. Confronta la rotta corrente con la rotta della schermata
                selected = currentRoute == screen.route,
                // 4. Al click, passa la rotta della schermata
                onClick = { onScreenSelected(screen.route) }
            )
        }
    }
}

// Aggiorniamo anche l'anteprima per riflettere le modifiche
@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(currentRoute = Screen.Home.route, onScreenSelected = {})
}