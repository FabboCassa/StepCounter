package com.app.stepcounter.presentation.ui.home

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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.presentation.viewmodel.PartyDetailViewModel

@Composable
fun PartyDetailScreen(viewModel: PartyDetailViewModel) {
    val party by viewModel.partyState.collectAsState()
    val context = LocalContext.current

    if (party == null) {
        // Mostra un caricamento
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Ordina i partecipanti per passi, dal maggiore al minore
    val sortedParticipants = party!!.participants.sortedByDescending { it.steps }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- 1. Grafico dei Passi ---
        Text("Classifica Passi", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        StepGraph(participants = sortedParticipants)

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. Lista Partecipanti ---
        Text("Partecipanti", style = MaterialTheme.typography.titleLarge)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(party!!.participants) { participant ->
                ParticipantRow(participant = participant)
            }
        }

        // --- 3. Pulsante Invita ---
        Button(
            onClick = { viewModel.inviteToParty(context) }, // <-- Chiama la funzione
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "Invita")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Invita Amici con un Link")
        }
    }
}

@Composable
fun StepGraph(participants: List<Participant>) {
    // Semplice rappresentazione con Box. Puoi renderla più complessa.
    // ... implementa la UI del grafico ...
}

@Composable
fun ParticipantRow(participant: Participant) {
    // Card per ogni partecipante con nome e passi
    // ... implementa la riga per il partecipante ...
}