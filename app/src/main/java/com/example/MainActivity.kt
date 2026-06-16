package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
                                }
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
}
