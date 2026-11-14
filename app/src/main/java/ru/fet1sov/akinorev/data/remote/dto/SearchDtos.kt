package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchItemDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val banner: String? = null,
    val icon: String? = null,
    val genre: CatalogGenreDto? = null,
    val developer: CatalogDeveloperDto? = null,
    val author: CatalogAuthorDto? = null,
    val platforms: List<CatalogPlatformDto> = emptyList(),
    val name: String = "",
    val handle: String = "",
    val avatar: String? = null,
    val website: String? = null,
    val username: String = "",
    val bio: String = "",
    @SerialName("_score") val score: Int = 0,
    @SerialName("_category") val category: String = "",
)

@Serializable
data class SearchCategoryDto(
    val category: String = "",
    val items: List<SearchItemDto> = emptyList(),
)

@Serializable
data class SearchPayloadDto(
    val query: String = "",
    val categories: List<SearchCategoryDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
)
