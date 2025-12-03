package com.example.sentio.data.repositories

import com.example.sentio.data.local.datasource.TagLocalDataSource
import com.example.sentio.data.mapper.toDomain
import com.example.sentio.data.mapper.toEntity
import com.example.sentio.domain.models.Tag
import com.example.sentio.domain.repositories.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository implementation for Tag operations.
 * Uses data sources for database access and mappers for conversion.
 */
class TagRepositoryImpl(
    private val tagDataSource: TagLocalDataSource
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> =
        tagDataSource.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getTagById(id: String): Tag? =
        tagDataSource.getById(id)?.toDomain()

    override suspend fun createTag(tag: Tag): Result<Tag> = runCatching {
        tagDataSource.insert(tag.toEntity())
        tag
    }

    override suspend fun updateTag(tag: Tag): Result<Tag> = runCatching {
        tagDataSource.update(tag.toEntity())
        tag
    }

    override suspend fun deleteTag(id: String): Result<Unit> = runCatching {
        tagDataSource.delete(id)
    }
}
