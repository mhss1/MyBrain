package com.mhss.app.mybrain.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun DonationItem(
    title: String,
    link: String,
    @DrawableRes
    icon: Int = 0
) {
    val context = LocalContext.current
    Row(
        Modifier
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(link)
                context.startActivity(intent)
            }
            .clip(RoundedCornerShape(25.dp))
            .border(
                color = Color.DarkGray,
                width = 1.dp,
                shape = RoundedCornerShape(25.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != 0) Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.padding(4.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(8.dp)
        )
    }
}