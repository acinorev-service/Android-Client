package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.fet1sov.akinorev.ui.components.SkeletonBlock
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun ProfileHeaderSkeleton(
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonBlock(height = 156.dp, width = 156.dp, shape = CircleShape)
        SkeletonBlock(height = 30.dp, width = 180.dp)
        SkeletonBlock(height = 18.dp, width = 220.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) {
                SkeletonBlock(height = 26.dp, width = 26.dp, shape = CircleShape)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        SkeletonBlock(height = 40.dp, width = 120.dp)
        SkeletonBlock(height = 18.dp, width = 90.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SkeletonBlock(height = 40.dp, width = 150.dp, shape = RoundedCornerShape(12.dp))
            SkeletonBlock(height = 40.dp, width = 44.dp, shape = CircleShape)
        }
        Spacer(modifier = Modifier.height(4.dp))
        repeat(2) {
            PostCardSkeleton(palette = palette)
        }
    }
}

@Composable
fun PostCardSkeleton(
    palette: NavigationPalette,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 4.dp))
                .background(palette.bar)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SkeletonBlock(height = 40.dp, width = 40.dp, shape = CircleShape)
                SkeletonBlock(height = 14.dp, width = 120.dp)
            }
            SkeletonBlock(height = 15.dp, width = 240.dp)
            SkeletonBlock(height = 15.dp, width = 190.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(height = 24.dp, width = 56.dp, shape = CircleShape)
                SkeletonBlock(height = 24.dp, width = 96.dp, shape = CircleShape)
                SkeletonBlock(height = 24.dp, width = 56.dp, shape = CircleShape)
            }
        }
        Row(modifier = Modifier.padding(start = 16.dp)) {
            SkeletonBlock(height = 12.dp, width = 180.dp)
        }
    }
}
