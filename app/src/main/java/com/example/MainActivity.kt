package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.StudyRepository
import com.example.ui.AlbumDetailScreen
import com.example.ui.AlbumsScreen
import com.example.ui.StudyViewModel
import com.example.ui.StudyViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private var sharedImageUrisState: MutableState<List<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = StudyRepository(database.studyDao())
        val viewModel = ViewModelProvider(
            this,
            StudyViewModelFactory(repository)
        )[StudyViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            val sharedImageUris = remember { mutableStateOf(readSharedImageUris(intent)) }
            sharedImageUrisState = sharedImageUris

            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "albums") {
                        composable("albums") {
                            AlbumsScreen(
                                viewModel = viewModel,
                                onAlbumClick = { albumId ->
                                    navController.navigate("album/$albumId")
                                },
                                pendingSharedImageUris = sharedImageUris.value,
                                onSharedImagesHandled = { sharedImageUris.value = emptyList() }
                            )
                        }
                        composable(
                            route = "album/{albumId}",
                            arguments = listOf(navArgument("albumId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val albumId = backStackEntry.arguments?.getInt("albumId") ?: return@composable
                            AlbumDetailScreen(
                                albumId = albumId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedImageUrisState?.value = readSharedImageUris(intent)
    }

    private fun readSharedImageUris(intent: Intent?): List<String> {
        if (intent == null) return emptyList()
        val type = intent.type ?: return emptyList()
        if (!type.startsWith("image/")) return emptyList()

        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                listOfNotNull(uri?.toString())
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                    ?.map { it.toString() }
                    ?: emptyList()
            }
            else -> emptyList()
        }
    }
}
