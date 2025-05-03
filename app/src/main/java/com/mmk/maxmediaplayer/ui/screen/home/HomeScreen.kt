package com.mmk.maxmediaplayer.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mmk.maxmediaplayer.ui.components.TrackListItem
import com.mmk.maxmediaplayer.ui.model.TrackItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onTrackClick: (String) -> Unit
) {
    val uiState = viewModel.uiState

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState.value) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HomeUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HomeUiState.Success -> {
                TrackList(
                    tracks = state.tracks,
                    onTrackClick = onTrackClick,
                    onRefresh = { viewModel.refreshData() }
                )
            }
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<TrackItem>,
    onTrackClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tracks.size) { index ->
            TrackListItem(
                track = tracks[index],
                onClick = { onTrackClick(tracks[index].id) }
            )
        }
    }
}