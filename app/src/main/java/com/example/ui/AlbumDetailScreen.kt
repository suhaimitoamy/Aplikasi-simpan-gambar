package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.StudyImage
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: Int,
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val albumFlow = remember(albumId) { viewModel.getAlbumById(albumId) }
    val imagesFlow = remember(albumId) { viewModel.getImagesForAlbum(albumId) }
    val album by albumFlow.collectAsStateWithLifecycle()
    val images by imagesFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var fullscreenImageIndex by remember { mutableStateOf<Int?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            val storedUris = uris.map { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                copyImageToInternalStorage(context, uri.toString()) ?: uri.toString()
            }
            viewModel.addImagesToAlbum(albumId, storedUris)
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    imagePickerLauncher.launch(arrayOf("image/*"))
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Add Image")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DETAIL TOPIK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = album?.title ?: "Loading...",
                        style = MaterialTheme.typography.displayMedium.copy(
                            lineHeight = 40.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "URUTAN GAMBAR: ${if(images.isNotEmpty()) "01 — ${images.size.toString().padStart(2, '0')}" else "0"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)
            )

            if (images.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Belum ada gambar. Tap FAB untuk menambah.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    itemsIndexed(images, key = { _, item -> item.id }) { index, studyImage ->
                        StudyImageItem(
                            studyImage = studyImage,
                            stepNumber = index + 1,
                            onImageClick = { fullscreenImageIndex = index }
                        )
                    }
                }
            }
        }
    }

    fullscreenImageIndex?.let { imageIndex ->
        if (images.isNotEmpty()) {
            val safeIndex = imageIndex.coerceIn(0, images.lastIndex)
            FullscreenImageDialog(
                imageUris = remember(images) { images.map { it.uriString } },
                currentIndex = safeIndex,
                onIndexChange = { fullscreenImageIndex = it },
                onDismiss = { fullscreenImageIndex = null }
            )
        }
    }
}

@Composable
fun StudyImageItem(
    studyImage: StudyImage,
    stepNumber: Int,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 250.dp, max = 400.dp)
                .clickable(onClick = onImageClick)
        ) {
            AsyncImage(
                model = studyImage.uriString,
                contentDescription = "Study Material",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = 220f
                        )
                    )
            )
            Text(
                text = stepNumber.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Black
                ),
                color = Color.White,
                modifier = Modifier.padding(start = 24.dp, top = 20.dp)
            )
        }
    }
}

@Composable
fun FullscreenImageDialog(
    imageUris: List<String>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val imageUri = imageUris.getOrNull(currentIndex) ?: return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(currentIndex, imageUris.size) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                        },
                        onDragEnd = {
                            if (totalDrag < -80f && currentIndex < imageUris.lastIndex) {
                                onIndexChange(currentIndex + 1)
                            }
                        }
                    )
                }
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Fullscreen Study Material",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

private fun copyImageToInternalStorage(context: Context, uriString: String): String? {
    return try {
        val sourceUri = Uri.parse(uriString)
        val dir = File(context.filesDir, "stored_images")
        if (!dir.exists()) dir.mkdirs()
        val extension = context.contentResolver.getType(sourceUri)
            ?.substringAfterLast("/")
            ?.substringBefore(";")
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"
        val targetFile = File(dir, "image_${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        Uri.fromFile(targetFile).toString()
    } catch (e: Exception) {
        null
    }
}
