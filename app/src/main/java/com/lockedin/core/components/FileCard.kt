package com.lockedin.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lockedin.core.utils.FileCategory
import com.lockedin.core.utils.FileUtils

@Composable
fun FileCard(
    fileName: String,
    mimeType: String,
    sizeBytes: Long,
    receivedAt: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fileCategory = FileUtils.getMimeTypeCategory(mimeType)
    val icon = when (fileCategory) {
        FileCategory.PDF -> Icons.Default.PictureAsPdf
        FileCategory.IMAGE -> Icons.Default.Image
        FileCategory.DOCUMENT -> Icons.Default.Description
        FileCategory.TEXT -> Icons.Default.TextSnippet
        FileCategory.OTHER -> Icons.Default.InsertDriveFile
    }
    val iconTint = when (fileCategory) {
        FileCategory.PDF -> MaterialTheme.colorScheme.error
        FileCategory.IMAGE -> MaterialTheme.colorScheme.secondary
        FileCategory.DOCUMENT -> MaterialTheme.colorScheme.primary
        FileCategory.TEXT -> MaterialTheme.colorScheme.tertiary
        FileCategory.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = fileCategory.name,
                tint = iconTint,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = FileUtils.formatFileSize(sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formatDate(receivedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
