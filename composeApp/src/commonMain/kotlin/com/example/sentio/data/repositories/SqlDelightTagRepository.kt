package com.example.sentio.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.sentio.db.SentioDatabase
import com.example.sentio.domain.models.Tag
import com.example.sentio.domain.repositories.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightTagRepository(
    private val database: SentioDatabase
) : TagRepository {

    private val queries = database.tagQueries

    override fun getAllTags(): Flow<List<Tag>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tags -> tags.map { it.toDomainModel() } }
    }

    override suspend fun getTagById(id: String): Tag? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomainModel()
    }

    override suspend fun createTag(tag: Tag): Result<Tag> = withContext(Dispatchers.IO) {
        try {
            queries.insert(tag.id, tag.name, tag.color)
            Result.success(tag)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTag(tag: Tag): Result<Tag> = withContext(Dispatchers.IO) {
        try {
            queries.update(tag.name, tag.color, tag.id)
            Result.success(tag)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTag(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            queries.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.example.sentio.db.Tag.toDomainModel() = Tag(
        id = id,
        name = name,
        color = color
    )
}
