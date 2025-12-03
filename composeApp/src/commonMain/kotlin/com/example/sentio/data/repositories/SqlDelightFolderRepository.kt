package com.example.sentio.data.repositories

import com.example.sentio.data.local.datasource.FolderLocalDataSource
import com.example.sentio.data.mapper.toDomain
import com.example.sentio.data.mapper.toEntity
import com.example.sentio.domain.models.Folder
import com.example.sentio.domain.repositories.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository implementation for Folder operations.
 * Uses data sources for database access and mappers for conversion.
 */
class FolderRepositoryImpl(
    private val folderDataSource: FolderLocalDataSource
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> =
        folderDataSource.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getRootFolders(): Flow<List<Folder>> =
        folderDataSource.observeRoots().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getSubFolders(parentId: String): Flow<List<Folder>> =
        folderDataSource.observeChildren(parentId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getFolderById(id: String): Folder? =
        folderDataSource.getById(id)?.toDomain()

    override suspend fun createFolder(folder: Folder): Result<Folder> = runCatching {
        folderDataSource.insert(folder.toEntity())
        folder
    }

    override suspend fun updateFolder(folder: Folder): Result<Folder> = runCatching {
        folderDataSource.update(folder.toEntity())
        folder
    }

    override suspend fun deleteFolder(id: String): Result<Unit> = runCatching {
        folderDataSource.delete(id)
    }
}
