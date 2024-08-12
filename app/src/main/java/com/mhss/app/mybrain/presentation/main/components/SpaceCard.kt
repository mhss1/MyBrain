package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R
import com.mhss.app.ui.theme.Blue

@Composable
fun SpaceCard(
    title: String,
    image: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(25.dp),
        elevation = CardDefaults.elevatedCardElevation(
            12.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            Modifier
                .clickable { onClick() }
                .aspectRatio(1.0f)
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall.copy(color = Color.White))
            Image(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.End),
                painter = painterResource(id = image),
                contentDescription = title)

        }
    }
}

@Preview
@Composable
fun SpaceCardPreview() {
    SpaceCard(
        "Notes",
        R.drawable.notes_img,
        Blue
    )
}