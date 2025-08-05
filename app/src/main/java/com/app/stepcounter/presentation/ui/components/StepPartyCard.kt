package com.app.stepcounter.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.stepcounter.domain.model.PartyData

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import com.app.stepcounter.domain.model.Participant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepPartyCard(
    modifier: Modifier = Modifier,
    party: PartyData? = null,
    onCreatePartyClick: (() -> Unit)? = null,
    onPartyClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            if (party == null) {
                onCreatePartyClick?.invoke()
            } else {
                onPartyClick?.invoke()
            }
        }
    ) {
        if (party != null) {
            // Card party esistente
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = party.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (party.participants.isNotEmpty()) {
                        Text(
                            text = "${party.participants.size} partecipanti",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Nessun partecipante",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Azioni
                Row {
                    IconButton(onClick = { onDeleteClick?.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina party",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Card "Crea party"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Party",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crea nuovo Party",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StepPartyCardPreview_WithParty() {
    val sampleParty = PartyData(
        id = "1",
        name = "Festa di Fabio",
        participants = listOf(Participant("1", "Fabio", 1000), Participant("2", "Luca", 500)),
        createdAt = System.currentTimeMillis()
    )
    StepPartyCard(
        party = sampleParty,
        onCreatePartyClick = {},
        onPartyClick = {},
        onDeleteClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun StepPartyCardPreview_CreateParty() {
    StepPartyCard(
        party = null,
        onCreatePartyClick = {},
        onPartyClick = {},
        onDeleteClick = {}
    )
}
