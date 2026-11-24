package com.example.hakot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hakot.R

@Composable
fun DumpTruckLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(130.dp, 75.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.hakot_logo),
            contentDescription = "Hakot Logo Truck",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )
    }
}
