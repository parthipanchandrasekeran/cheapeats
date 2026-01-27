package com.parthipan.cheapeats.ui.favorites

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.parthipan.cheapeats.data.favorites.Collection
import com.parthipan.cheapeats.data.favorites.CollectionWithCount

/**
 * Dialog for adding a restaurant to collections.
 */
@Composable
fun CollectionPickerDialog(
    restaurantName: String,
    currentCollections: List<Collection>,
    allCollections: List<CollectionWithCount>,
    onToggleCollection: (Collection, Boolean) -> Unit,
    onCreateCollection: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Collection") },
        text = {
            Column {
                Text(
                    text = restaurantName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allCollections) { collectionWithCount ->
                        val isSelected = currentCollections.any {
                            it.id == collectionWithCount.collection.id
                        }

                        CollectionToggleRow(
                            collection = collectionWithCount.collection,
                            count = collectionWithCount.restaurantCount,
                            isSelected = isSelected,
                            onToggle = {
                                onToggleCollection(collectionWithCount.collection, !isSelected)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Collection")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )

    if (showCreateDialog) {
        CreateCollectionDialog(
            onCreate = { name ->
                onCreateCollection(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun CollectionToggleRow(
    collection: Collection,
    count: Int,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val iconColor = try {
        Color(AndroidColor.parseColor(collection.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Collection icon with color
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = collection.icon.toCollectionIcon(),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$count restaurants",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Checkbox
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun CreateCollectionDialog(
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Collection") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isError = false
                },
                label = { Text("Collection name") },
                isError = isError,
                supportingText = if (isError) {
                    { Text("Name must be at least 2 characters") }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.length >= 2) {
                        onCreate(name)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Convert icon name string to ImageVector.
 */
fun String.toCollectionIcon(): ImageVector {
    return when (this.lowercase()) {
        "favorite" -> Icons.Default.Favorite
        "lunch_dining" -> Icons.Default.Place
        "nightlife" -> Icons.Default.Star
        "eco" -> Icons.Default.Favorite
        "bolt" -> Icons.Default.Star
        else -> Icons.Default.List
    }
}
