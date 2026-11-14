package ru.fet1sov.akinorev.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.model.UserProfileSummary
import ru.fet1sov.akinorev.ui.format.formatMoney
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun ProfileHeader(
    profile: UserProfileSummary?,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isLoading) {
        ProfileHeaderSkeleton(modifier)
        return
    }

    val user = profile ?: return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        UserAvatar(
            avatar = user.avatar,
            userId = user.id,
            username = user.username,
            size = 64.dp,
            isSubscribed = user.isSubscribed,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            ProfileName(
                username = user.username,
                nameColor = user.nameColor,
                nameAnimation = user.nameAnimation,
                fontSize = 24.sp,
            )
            Text(
                text = formatMoney(user.wallet),
                color = navigationPalette().accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ProfileHeaderSkeleton(modifier: Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBlock(height = 64.dp, width = 64.dp, shape = RoundedCornerShape(10.dp))

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            SkeletonBlock(height = 22.dp, width = 160.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBlock(height = 18.dp, width = 110.dp)
        }
    }
}
