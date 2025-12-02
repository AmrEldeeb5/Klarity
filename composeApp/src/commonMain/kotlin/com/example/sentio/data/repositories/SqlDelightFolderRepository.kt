package com.example.sentio.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.sentio.db.SentioDatabase
import com.example.sentio.domain.models.Folder
import com.example.sentio.domain.repositories.FolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class SqlDelightFolderRepository(
    private val database: SentioDatabase
) : FolderRepository {

    private val queries = database.folderQueries

    override fun getAllFolders(): Flow<List<Folder>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { folders -> folders.map { it.toDomainModel() } }
    }

    override fun getRootFolders(): Flow<List<Folder>> {
        return queries.selectRoots()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { folders -> folders.map { it.toDomainModel() } }
    }

    override fun getSubFolders(parentId: String): Flow<List<Folder>> {
        return queries.selectChildren(parentId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { folders -> folders.map { it.toDomainModel() } }
    }

    override suspend fun getFolderById(id: String): Folder? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomainModel()
    }

    override suspend fun createFolder(folder: Folder): Result<Folder> = withContext(Dispatchers.IO) {
        try {
            queries.insert(
                id = folder.id,
                name = folder.name,
                parentId = folder.parentId,
                createdAt = folder.createdAt.toEpochMilliseconds(),
                icon = folder.icon
            )
            Result.success(folder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFolder(folder: Folder): Result<Folder> = withContext(Dispatchers.IO) {
        try {
            queries.update(
                name = folder.name,
                parentId = folder.parentId,
                icon = folder.icon,
                id = folder.id
            )
            Result.success(folder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFolder(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            queries.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.example.sentio.db.Folder.toDomainModel() = Folder(
        id = id,
        name = name,
        parentId = parentId,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        icon = icon
    )
}
