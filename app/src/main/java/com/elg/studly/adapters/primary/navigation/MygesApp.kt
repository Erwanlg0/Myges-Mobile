package com.elg.studly.adapters.primary.navigation

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elg.studly.R
import com.elg.studly.adapters.primary.ui.AbsencesScreen
import com.elg.studly.adapters.primary.ui.AgendaScreen
import com.elg.studly.adapters.primary.ui.AuthRoute
import com.elg.studly.adapters.primary.ui.CoursesScreen
import com.elg.studly.adapters.primary.ui.DashboardScreen
import com.elg.studly.adapters.primary.ui.DepositWebViewScreen
import com.elg.studly.adapters.primary.ui.DirectoryScreen
import com.elg.studly.adapters.primary.ui.DocumentsScreen
import com.elg.studly.adapters.primary.ui.GradesScreen
import com.elg.studly.adapters.primary.ui.EventsScreen
import com.elg.studly.adapters.primary.ui.NotificationsScreen
import com.elg.studly.adapters.primary.ui.PracticalsScreen
import com.elg.studly.adapters.primary.ui.ProjectsScreen
import com.elg.studly.adapters.primary.ui.SettingsScreen
import com.elg.studly.adapters.primary.ui.StudentAvatar
import com.elg.studly.adapters.secondary.pdf.writeToDownloads
import com.elg.studly.adapters.primary.viewmodel.AppViewModel
import com.elg.studly.adapters.primary.viewmodel.DocumentOpenRequest
import com.elg.studly.adapters.primary.viewmodel.SettingsViewModel
import com.elg.studly.adapters.primary.viewmodel.StudentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Destination(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector
)

private suspend fun saveToDownloads(context: Context, uri: Uri, mimeType: String?): Boolean = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val displayName = runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/') ?: "document"
    writeToDownloads(context, displayName, mimeType) { output ->
        resolver.openInputStream(uri)?.use { input -> input.copyTo(output) }
            ?: error("source stream unavailable")
    } != null
}

private val destinations = listOf(
    Destination("dashboard", R.string.dashboard_title, Icons.Rounded.Home),
    Destination("agenda", R.string.agenda_title, Icons.Rounded.Event),
    Destination("grades", R.string.grades_title, Icons.Rounded.Grade),
    Destination("absences", R.string.absences_title, Icons.Rounded.Warning),
    Destination("courses", R.string.courses_title, Icons.Rounded.MenuBook),
    Destination("projects", R.string.projects_title, Icons.Rounded.Work),
    Destination("practicals", R.string.practicals_title, Icons.Rounded.Science),
    Destination("documents", R.string.documents_title, Icons.Rounded.Description),
    Destination("directory", R.string.directory_title, Icons.Rounded.People),
    Destination("events", R.string.events_title, Icons.Rounded.Celebration),
    Destination("notifications", R.string.notifications_title, Icons.Rounded.Notifications),
    Destination("settings", R.string.settings_title, Icons.Rounded.Settings)
)

@Composable
fun MygesApp(
    oauthCallbackUri: Uri? = null,
    onOAuthCallbackConsumed: () -> Unit = {},
    notificationRoute: String? = null,
    onNotificationRouteConsumed: () -> Unit = {},
    onSuccessfulRefresh: (Activity) -> Unit = {}
) {
    val viewModel: AppViewModel = hiltViewModel()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    LaunchedEffect(settings?.languageTag, settings != null) {
        val currentSettings = settings ?: return@LaunchedEffect
        AppCompatDelegate.setApplicationLocales(
            currentSettings.languageTag?.let(LocaleListCompat::forLanguageTags)
                ?: LocaleListCompat.getEmptyLocaleList()
        )
    }
    LaunchedEffect(session, oauthCallbackUri) {
        if (session != null && oauthCallbackUri != null) onOAuthCallbackConsumed()
    }
    if (session == null) {
        AuthRoute(
            oauthCallbackUri = oauthCallbackUri,
            onOAuthCallbackConsumed = onOAuthCallbackConsumed
        )
    } else {
        StudentRoute(notificationRoute, onNotificationRouteConsumed, onSuccessfulRefresh)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentRoute(
    notificationRoute: String?,
    onNotificationRouteConsumed: () -> Unit,
    onSuccessfulRefresh: (Activity) -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val studentViewModel: StudentViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var pendingDocumentSave by remember { mutableStateOf<DocumentOpenRequest?>(null) }
    val saveDocument: (DocumentOpenRequest) -> Unit = { request ->
        coroutineScope.launch {
            if (saveToDownloads(context, request.uri, request.mimeType)) {
                Toast.makeText(context, R.string.documents_saved_to_downloads, Toast.LENGTH_LONG).show()
            } else {
                studentViewModel.reportOpenDocumentFailure()
            }
        }
    }
    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val request = pendingDocumentSave
        pendingDocumentSave = null
        if (granted && request != null) saveDocument(request)
        else if (request != null) studentViewModel.reportOpenDocumentFailure()
    }
    val requestDocumentSave: (DocumentOpenRequest) -> Unit = { request ->
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.O..Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingDocumentSave = request
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            saveDocument(request)
        }
    }
    val dashboardState by studentViewModel.dashboard.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedDestination = destinations.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.route?.substringBefore('?') == destination.route } == true
    } ?: destinations.first()

    LaunchedEffect(studentViewModel) {
        studentViewModel.refreshSucceeded.collect {
            (context as? Activity)?.let(onSuccessfulRefresh)
        }
    }

    LaunchedEffect(studentViewModel) {
        studentViewModel.documentOpenRequests.collect { request ->
            if (request.downloadOnly) {
                requestDocumentSave(request)
                return@collect
            }
            val openWithMime = { mime: String ->
                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(request.uri, mime)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (intent.resolveActivity(context.packageManager) == null) {
                    throw ActivityNotFoundException("No app can open $mime")
                }
                context.startActivity(Intent.createChooser(intent, null))
            }
            try {
                openWithMime(request.mimeType ?: "application/octet-stream")
            } catch (exception: ActivityNotFoundException) {
                try {
                    val fallbackMimeType = when {
                        request.mimeType?.startsWith("text/") == true && request.mimeType != "text/plain" -> "text/plain"
                        request.mimeType != "application/octet-stream" -> "application/octet-stream"
                        else -> null
                    }
                    if (fallbackMimeType != null) {
                        openWithMime(fallbackMimeType)
                    } else {
                        throw exception
                    }
                } catch (e: ActivityNotFoundException) {
                    requestDocumentSave(request)
                }
            }
        }
    }

    LaunchedEffect(studentViewModel) {
        studentViewModel.depositRequests.collect { groupId ->
            navController.navigate("deposit/$groupId") { launchSingleTop = true }
        }
    }

    LaunchedEffect(notificationRoute) {
        val fullRoute = notificationRoute ?: return@LaunchedEffect
        val baseRoute = fullRoute.substringBefore('?')
        val destination = destinations.firstOrNull { it.route == baseRoute }
        if (destination != null) {
            navController.navigate(fullRoute) {
                popUpTo(destinations.first().route) { saveState = true }
            }
        }
        onNotificationRouteConsumed()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth >= 840.dp) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail {
                    destinations.forEach { destination ->
                        NavigationRailItem(
                            selected = selectedDestination.route == destination.route,
                            onClick = { navController.navigateTo(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.title)) }
                        )
                    }
                }
                StudentScaffold(
                    navController = navController,
                    selectedDestination = selectedDestination,
                    showNavigationIcon = false,
                    onNavigationClick = {},
                    studentViewModel = studentViewModel,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        dashboardState.data?.profile?.let { profile ->
                            StudentAvatar(
                                avatarUrl = profile.avatarUrl,
                                displayName = profile.displayName,
                                modifier = Modifier
                                    .padding(start = 24.dp, top = 24.dp)
                                    .size(56.dp)
                            )
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = 24.dp, top = 12.dp, end = 24.dp)
                            )
                        } ?: Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(24.dp)
                        )
                        destinations.forEach { destination ->
                            NavigationDrawerItem(
                                selected = selectedDestination.route == destination.route,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigateTo(destination.route)
                                },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(stringResource(destination.title)) }
                            )
                        }
                    }
                }
            ) {
                StudentScaffold(
                    navController = navController,
                    selectedDestination = selectedDestination,
                    showNavigationIcon = true,
                    onNavigationClick = { coroutineScope.launch { drawerState.open() } },
                    studentViewModel = studentViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateTo(route: String) {
    val isDeepLink = route.contains('?')
    navigate(route) {
        launchSingleTop = true
        restoreState = !isDeepLink
        popUpTo(destinations.first().route) {
            saveState = !isDeepLink
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentScaffold(
    navController: androidx.navigation.NavHostController,
    selectedDestination: Destination,
    showNavigationIcon: Boolean,
    onNavigationClick: () -> Unit,
    studentViewModel: StudentViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(selectedDestination.title)) },
                navigationIcon = {
                    if (showNavigationIcon) {
                        IconButton(onClick = onNavigationClick) {
                            Icon(
                                Icons.Rounded.Menu,
                                contentDescription = stringResource(R.string.cd_open_navigation)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = destinations.first().route,
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") { DashboardScreen(studentViewModel, onNavigateToTab = { route -> navController.navigateTo(route) }) }
            composable("agenda?id={id}") { backStackEntry ->
                AgendaScreen(studentViewModel, settingsViewModel, highlightedEventId = backStackEntry.arguments?.getString("id"))
            }
            composable("grades?id={id}") { backStackEntry ->
                GradesScreen(studentViewModel, highlightedGradeId = backStackEntry.arguments?.getString("id"))
            }
            composable("absences?id={id}") { backStackEntry ->
                AbsencesScreen(studentViewModel, highlightedAbsenceId = backStackEntry.arguments?.getString("id"))
            }
            composable("courses") { CoursesScreen(studentViewModel) }
            composable("projects?id={id}") { backStackEntry ->
                ProjectsScreen(studentViewModel, highlightedProjectId = backStackEntry.arguments?.getString("id"))
            }
            composable("practicals") { PracticalsScreen(studentViewModel) }
            composable("documents?id={id}") { backStackEntry ->
                DocumentsScreen(studentViewModel, highlightedDocumentId = backStackEntry.arguments?.getString("id"))
            }
            composable("deposit/{groupId}") { backStackEntry ->
                DepositWebViewScreen(
                    groupId = backStackEntry.arguments?.getString("groupId").orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable("directory") { DirectoryScreen(studentViewModel) }
            composable("events") { EventsScreen(studentViewModel) }
            composable("notifications") { NotificationsScreen(studentViewModel) }
            composable("settings") { SettingsScreen(settingsViewModel, studentViewModel) }
        }
    }
}
