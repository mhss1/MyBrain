package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mhss.app.ui.R
import com.mhss.app.mybrain.presentation.main.components.SpaceCard
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.PrimaryColor
import com.mhss.app.ui.theme.Purple
import com.mhss.app.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacesScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            MyBrainAppBar(stringResource(R.string.spaces))
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(180.dp),
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(spaces) { (title, image, color, screen) ->
                SpaceCard(
                    title = stringResource(title),
                    image = image,
                    backgroundColor = color,
                    onClick = {
                        navController.navigate(screen)
                    }
                )
            }
        }
    }
}


private val spaces = listOf(
    Space(R.string.notes, R.drawable.notes_img, Blue, Screen.NotesScreen),
    Space(R.string.tasks, R.drawable.tasks_img, Red, Screen.TasksScreen()),
    Space(R.string.diary, R.drawable.diary_img, Green, Screen.DiaryScreen),
    Space(R.string.bookmarks, R.drawable.bookmarks_img, Orange, Screen.BookmarksScreen),
    Space(R.string.calendar, R.drawable.calendar_img, Purple, Screen.CalendarScreen),
    // TODO: add actual assistant image and color
    Space(R.string.assistant, R.drawable.tasks_img, PrimaryColor, Screen.AssistantScreen)
)

private data class Space(
    val title: Int,
    val image: Int,
    val color: Color,
    val route: Screen
)

@Preview
@Composable
fun SpacesScreenPreview() {
    MyBrainTheme {
        SpacesScreen(
            navController = rememberNavController()
        )
    }
}