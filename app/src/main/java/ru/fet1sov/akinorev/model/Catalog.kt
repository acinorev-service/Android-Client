package ru.fet1sov.akinorev.model

data class CatalogGenre(
    val id: String,
    val slug: String,
    val titleKey: String,
)

data class CatalogGame(
    val id: String,
    val title: String,
    val description: String,
    val bannerPath: String,
    val iconPath: String,
    val genreSlug: String,
    val genreTitle: String,
    val developerId: String,
    val developerName: String,
    val developerAvatarPath: String,
    val authorUsername: String,
    val platformSlugs: List<String>,
    val publishedAtMillis: Long?,
) {
    val supportsMobile: Boolean
        get() = platformSlugs.isEmpty() || platformSlugs.contains(PlatformMobile)

    val supportsPc: Boolean
        get() = platformSlugs.isEmpty() || platformSlugs.contains(PlatformPc)
}

const val PlatformPc = "pc"
const val PlatformMobile = "mobile"
