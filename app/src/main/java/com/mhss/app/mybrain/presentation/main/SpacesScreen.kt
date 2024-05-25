package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.main.components.SpaceRegularCard
import com.mhss.app.mybrain.presentation.main.components.SpaceWideCard
import com.mhss.app.mybrain.presentation.navigation.Screen
import com.mhss.app.mybrain.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacesScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.spaces),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            item {
                Row {
                    SpaceRegularCard(
                        modifier = Modifier.weight(1f, fill = true),
                        title = stringResource(R.string.notes),
                        image = R.drawable.notes_img,
                        backgroundColor = Blue
                    ) {
                        navController.navigate(Screen.NotesScreen)
                    }
                    SpaceRegularCard(
                        modifier = Modifier.weight(1f, fill = true),
                        title = stringResource(R.string.tasks),
                        image = R.drawable.tasks_img,
                        backgroundColor = Red
                    ) {
                        navController.navigate(
                            Screen.TasksScreen()
                        )
                    }
                }
            }
            item {
                Row {
                    SpaceRegularCard(
                        modifier = Modifier.weight(1f, fill = true),
                        title = stringResource(R.string.diary),
                        image = R.drawable.diary_img,
                        backgroundColor = Green
                    ) {
                        navController.navigate(Screen.DiaryScreen)
                    }
                    SpaceRegularCard(
                        modifier = Modifier.weight(1f, fill = true),
                        title = stringResource(R.string.bookmarks),
                        image = R.drawable.bookmarks_img,
                        backgroundColor = Orange
                    ) {
                        navController.navigate(Screen.BookmarksScreen)
                    }
                }
            }
            item {
                SpaceWideCard(
                    title = stringResource(R.string.calendar),
                    image = R.drawable.calendar_img,
                    backgroundColor = Purple
                ) {
                    navController.navigate(Screen.CalendarScreen)
                }
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@Preview
@Composable
fun SpacesScreenPreview() {
    SpacesScreen(
        navController = rememberNavController()
    )
}