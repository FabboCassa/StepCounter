package com.app.stepcounter.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.sp
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.presentation.ui.components.ParticipantCard
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

            // --- Sezione 1: Grafico dei Passi ---
            Text("Classifica", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            StepGraph(participants = sortedParticipants)

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
                onClick = { viewModel.inviteToParty(context) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Invita")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invita Amici con un Link")
            }
        }
    }
}

@Composable
fun StepGraph(participants: List<Participant>) {
    // Il massimo dei passi serve per calcolare la proporzione delle barre
    val maxSteps = participants.maxOfOrNull { it.steps }?.toFloat() ?: 1f

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        if (participants.isEmpty()) {
            Text("Nessun partecipante ancora.")
        } else {
            // Mostriamo solo i primi 5 per non affollare il grafico
            participants.take(5).forEachIndexed { index, participant ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(
                    IntrinsicSize.Min)) {
                    // Icona per il primo classificato
                    if (index == 0 && participant.steps > 0) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Leader",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(32.dp)) // Spazio per allineare
                    }

                    Text(
                        text = participant.name,
                        modifier = Modifier.width(80.dp),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    // Barra del grafico proporzionale
                    val barPercentage = if (maxSteps > 0) participant.steps / maxSteps else 0f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = barPercentage)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                        )
                    }
                    Text(
                        text = "${participant.steps}",
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

