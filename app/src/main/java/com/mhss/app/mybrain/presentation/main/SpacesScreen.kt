package com.mhss.app.mybrain.presentation.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mhss.app.ui.R
import com.mhss.app.mybrain.presentation.main.components.SpaceCard
import com.mhss.app.presentation.components.drawAiGradientRadials
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.DarkGray
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.Purple
import com.mhss.app.ui.theme.Red

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
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(
                top = 10.dp,
                bottom = 32.dp,
                start = 10.dp,
                end = 10.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
            item {
                SpaceCard(
                    title = stringResource(R.string.assistant),
                    image = R.drawable.ai_chat_img,
                    backgroundColor = Color.Transparent,
                    onClick = {
                        navController.navigate(Screen.AssistantScreen)
                    },
                    contentModifier = Modifier.drawBehind {
                        drawAiGradientRadials(
                            background = DarkGray,
                            backgroundAlpha = 0.3f
                        )
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
)

private data class Space(
    val title: Int,
    val image: Int,
    val color: Color,
    val route: Screen
)

@Preview(widthDp = 360, heightDp = 680)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SpacesScreenPreview() {
    MyBrainTheme {
        SpacesScreen(
            navController = rememberNavController()
        )
    }
}