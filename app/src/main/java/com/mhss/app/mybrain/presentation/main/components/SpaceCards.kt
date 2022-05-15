package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.ui.theme.Blue

@Composable
fun SpaceRegularCard(
    title: String,
    image: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(25.dp),
        backgroundColor = backgroundColor,
        elevation = 12.dp
    ) {
        Column(
            Modifier
                .clickable { onClick() }
                .aspectRatio(1.0f)
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.h5.copy(color = Color.White))
            Image(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.End),
                painter = painterResource(id = image),
                contentDescription = title)

        }
    }
}

@Composable
fun SpaceWideCard(
    title: String,
    image: Int,
    backgroundColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(25.dp),
        backgroundColor = backgroundColor,
        elevation = 12.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(18.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h4.copy(color = Color.White),
            )
            Spacer(Modifier.height(12.dp))
            Image(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.End),
                painter = painterResource(id = image),
                contentDescription = title)

        }
    }
}

@Preview
@Composable
fun SpaceRegularCardPreview() {
    SpaceRegularCard(
        "Notes",
        R.drawable.notes_img,
        Blue
    )
}