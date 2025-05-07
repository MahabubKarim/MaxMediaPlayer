package com.mmk.maxmediaplayer.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.ui.components.MiniPlayer
import com.mmk.maxmediaplayer.ui.components.TrackListItem

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    onTrackClick: (Track) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()

    // which tab is selected
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Playlists", "Liked Songs", "Downloads")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("My Music") },
                navigationIcon = {
                    IconButton(onClick = { /* handle back */ }) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* handle more */ }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                }
            )
        },
        bottomBar = {
            MiniPlayer(
                viewModel = viewModel,
                navController = navController,
                track = currentTrack
            )
        }
    ) { innerPadding ->
        Column(Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = {
                            selectedTab = i
                        },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            Box(Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    is HomeUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurface
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
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    val tracks = listOf(
        Track("1", "Track 1", "Artist 1", 5, "03:30", imageUrl = ""),
        Track("2", "Track 2", "Artist 2", 5, "04:15", imageUrl = ""),
    )
    TrackList(
        tracks = tracks, onTrackClick = {}, onRefresh = {}
    )
}


@Composable
private fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(tracks.size) { idx ->
            TrackListItem(
                track = tracks[idx],
                onClick = { onTrackClick(tracks[idx]) },
            )
        }
    }
}
