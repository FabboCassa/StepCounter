package com.app.stepcounter.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.stepcounter.presentation.ui.components.ParticipantCard
import com.app.stepcounter.presentation.ui.components.PartyProgressChart
import com.app.stepcounter.presentation.viewmodel.PartyDetailViewModel

@Composable
fun PartyDetailScreen(viewModel: PartyDetailViewModel,  currentSteps: Int) {
    // Osserviamo lo stato del party dal ViewModel
    val partyState by viewModel.partyState.collectAsState()
    val currentParty = partyState
    val context = LocalContext.current

    LaunchedEffect(currentSteps) {
        viewModel.updateMySteps(currentSteps)
    }

    // Mostra un indicatore di caricamento finché non arrivano i dati del party dal server
    if (currentParty == null) {
        // Se 'party' è null, mostriamo un indicatore di caricamento.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            Text(
                "Caricamento dei dati del party...",
                modifier = Modifier.padding(top = 80.dp)
            )
        }
    } else {

        // Ordina i partecipanti per passi, dal maggiore al minore
        val sortedParticipants = currentParty .participants.sortedByDescending { it.steps }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Titolo del Party ---
            Text(
                text = currentParty.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(16.dp))
            Text("Codice Invito", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentParty.inviteCode ?: "Errore",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- Sezione 1: Grafico dei Passi ---
            Text("Classifica", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            PartyProgressChart(participants = sortedParticipants)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Sezione 2: Lista Partecipanti ---
            Text("Partecipanti", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentParty.participants) { participant ->
                    ParticipantCard(participant = participant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Sezione 3: Pulsante Invita ---
            Button(
                // Chiameremo una nuova funzione più appropriata
                onClick = { viewModel.shareInviteCode(context) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Condividi")
                Spacer(modifier = Modifier.width(8.dp))
                // ✅ Aggiorniamo il testo
                Text("Condividi Codice Invito")
            }

        }
    }
}

