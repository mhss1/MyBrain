package com.mhss.app.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.NavHostController
import com.mhss.app.ui.R
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.ui.components.common.MyBrainAppBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookmarkDetailsScreen(
    navController: NavHostController,
    bookmarkId: Int,
    viewModel: BookmarkDetailsViewModel = koinViewModel(parameters = { parametersOf(bookmarkId) }),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val state = viewModel.bookmarkDetailsUiState
    val snackbarHostState = remember { SnackbarHostState() }
    var openDialog by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var title by rememberSaveable { mutableStateOf(state.bookmark?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(state.bookmark?.description ?: "") }
    var url by rememberSaveable { mutableStateOf(state.bookmark?.url ?: "") }

    LaunchedEffect(state.bookmark) {
        if (state.bookmark != null) {
            title = state.bookmark.title
            description = state.bookmark.description
            url = state.bookmark.url
        }
    }
    LaunchedEffect(state) {
        if (state.navigateUp) {
            openDialog = false
            navController.navigateUp()
        }
        if (state.error != null) {
            snackbarHostState.showSnackbar(
                context.getString(state.error)
            )
            viewModel.onEvent(BookmarkDetailsEvent.ErrorDisplayed)
        }
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            viewModel.onEvent(
                BookmarkDetailsEvent.ScreenOnStop(
                    Bookmark(
                        title = title,
                        description = description,
                        url = url
                    )
                )
            )
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MyBrainAppBar(
                title = "",
                actions = {
                    if (state.bookmark != null) IconButton(onClick = { openDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_bookmark)
                        )
                    }
                    IconButton(onClick = {
                        if (url.isValidUrl()) {
                            uriHandler.openUri(if (!url.startsWith("https://") && !url.startsWith("http://")) "http://$url" else url)
                        } else scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.invalid_url)
                            )
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_open_link),
                            contentDescription = stringResource(R.string.open_link),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(text = stringResource(R.string.url)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(text = stringResource(R.string.title)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = stringResource(R.string.description)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (openDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDialog = false },
                title = { Text(stringResource(R.string.delete_bookmark_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.delete_bookmark_confirmation_message
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(BookmarkDetailsEvent.DeleteBookmark(state.bookmark!!))
                        },
                    ) {
                        Text(stringResource(R.string.delete_bookmark), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            openDialog = false
                        }) {
                        Text(stringResource(R.string.cancel), color = Color.White)
                    }
                }
            )
    }
}