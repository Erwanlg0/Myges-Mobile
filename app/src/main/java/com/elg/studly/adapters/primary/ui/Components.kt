package com.elg.studly.adapters.primary.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.elg.studly.R
import com.elg.studly.adapters.primary.state.FeatureUiState
import com.elg.studly.adapters.primary.state.messageRes
import com.elg.studly.domain.model.AppError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FeatureStateContent(
    state: FeatureUiState<T>,
    empty: (T) -> Boolean,
    @StringRes emptyTitle: Int,
    @StringRes emptyBody: Int,
    onRetry: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.(T) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.refreshing && !empty(state.data),
        onRefresh = onRetry,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            (state.loading || state.refreshing) && empty(state.data) -> LoadingState(firstSync = true)
            !state.online && empty(state.data) -> StatusScreen(
                icon = Icons.Rounded.WifiOff,
                title = stringResource(R.string.state_offline_empty_title),
                body = stringResource(R.string.state_offline_empty_body),
                onRetry = onRetry
            )
            state.error != null && empty(state.data) -> ErrorState(state.error, onRetry)
            empty(state.data) -> StatusScreen(
                icon = Icons.Rounded.Inbox,
                title = stringResource(emptyTitle),
                body = stringResource(emptyBody),
                onRetry = onRetry
            )
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!state.online) {
                    item { StateBanner(R.string.state_offline) }
                }
                if (state.error != null) {
                    item { StateBanner(state.error) }
                }
                content(state.data)
            }
        }
    }
}

@Composable
fun LoadingState(firstSync: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.state_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (firstSync) {
                Text(
                    text = stringResource(R.string.state_loading_first_sync),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CenteredState(
    icon: ImageVector,
    title: String,
    body: String,
    onRetry: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    filledButton: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = iconTint
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium
            )
            val retryContent: @Composable RowScope.() -> Unit = {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_retry))
            }
            if (filledButton) {
                Button(onClick = onRetry, content = retryContent)
            } else {
                OutlinedButton(onClick = onRetry, content = retryContent)
            }
        }
    }
}

@Composable
fun OfflineState(onRetry: () -> Unit) {
    CenteredState(
        icon = Icons.Rounded.WifiOff,
        title = stringResource(R.string.state_offline_empty_title),
        body = stringResource(R.string.state_offline_empty_body),
        onRetry = onRetry
    )
}

@Composable
fun EmptyState(
    @StringRes title: Int,
    @StringRes body: Int,
    onRetry: () -> Unit
) {
    CenteredState(
        icon = Icons.Rounded.Inbox,
        title = stringResource(title),
        body = stringResource(body),
        onRetry = onRetry
    )
}

@Composable
fun ErrorState(
    error: AppError,
    onRetry: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(error) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    CenteredState(
        icon = Icons.Rounded.ErrorOutline,
        title = stringResource(R.string.state_error_title),
        body = error.displayText(),
        onRetry = onRetry,
        iconTint = MaterialTheme.colorScheme.error,
        filledButton = true
    )
        filledButton = true
    )
}

@Composable
fun AppError.displayText(): String = when (this) {
    is AppError.Remote -> {
        val base = stringResource(R.string.error_remote)
        val detail = listOfNotNull(
            code?.let { "HTTP $it" },
            message?.takeIf { it.isNotBlank() }
        ).joinToString(": ")
        if (detail.isNotEmpty()) "$base ($detail)" else base
    }
    is AppError.Unexpected -> {
        val base = stringResource(R.string.error_unexpected)
        if (!message.isNullOrBlank()) "$base ($message)" else base
    }
    else -> stringResource(messageRes())
}
    }
    is AppError.Unexpected -> {
        val base = stringResource(R.string.error_unexpected)
        if (!message.isNullOrBlank()) "$base ($message)" else base
    }
    else -> stringResource(messageRes())
}

@Composable
fun StateBanner(@StringRes text: Int) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(text) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = stringResource(text),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun StateBanner(error: AppError) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(error) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = error.displayText(),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun RefreshingRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
        Text(
            text = stringResource(R.string.state_refreshing),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DataCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
fun CompactCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

@Composable
fun LabelValue(
    @StringRes label: Int,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    if (value.isBlank()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (onClick != null) TextDecoration.Underline else TextDecoration.None,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun String.orUntitled(): String {
    return ifBlank { stringResource(R.string.common_untitled) }
}
