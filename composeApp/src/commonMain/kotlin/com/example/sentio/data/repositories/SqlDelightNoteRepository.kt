package com.example.sentio.data.repositories

import com.example.sentio.data.local.datasource.NoteLocalDataSource
import com.example.sentio.data.local.datasource.TagLocalDataSource
import com.example.sentio.data.mapper.toDomain
import com.example.sentio.data.mapper.toEntity
import com.example.sentio.domain.models.Note
import com.example.sentio.domain.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository implementation for Note operations.
 * Uses data sources for database access and mappers for conversion.
 */
class NoteRepositoryImpl(
    private val noteDataSource: NoteLocalDataSource,
    private val tagDataSource: TagLocalDataSource
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDataSource.observeAll().map { entities ->
            entities.map { entity ->
                val tags = tagDataSource.observeByNote(entity.id).first().map { it.name }
                entity.toDomain(tags)
            }
        }

    override suspend fun getNoteById(id: String): Note? {
        val entity = noteDataSource.getById(id) ?: return null
        val tags = tagDataSource.observeByNote(id).first().map { it.name }
        return entity.toDomain(tags)
    }

    override fun getNotesByFolder(folderId: String): Flow<List<Note>> =
        noteDataSource.observeByFolder(folderId).map { entities ->
            entities.map { entity ->
                val tags = tagDataSource.observeByNote(entity.id).first().map { it.name }
                entity.toDomain(tags)
            }
        }

    override fun getNotesByTag(tagId: String): Flow<List<Note>> =
        getAllNotes().map { notes ->
            notes.filter { note -> note.tags.contains(tagId) }
        }

    override fun getPinnedNotes(): Flow<List<Note>> =
        noteDataSource.observePinned().map { entities ->
            entities.map { entity ->
                val tags = tagDataSource.observeByNote(entity.id).first().map { it.name }
                entity.toDomain(tags)
            }
        }

    override fun getFavoriteNotes(): Flow<List<Note>> =
        noteDataSource.observeFavorites().map { entities ->
            entities.map { entity ->
                val tags = tagDataSource.observeByNote(entity.id).first().map { it.name }
                entity.toDomain(tags)
            }
        }

    override suspend fun createNote(note: Note): Result<Note> = runCatching {
        noteDataSource.insert(note.toEntity())
        
        // Link tags
        note.tags.forEach { tagName ->
            tagDataSource.linkToNote(note.id, tagName)
        }
        
        note
    }

    override suspend fun updateNote(note: Note): Result<Note> = runCatching {
        noteDataSource.update(note.toEntity())
        
        // Update tags: remove old, add new
        tagDataSource.unlinkAllFromNote(note.id)
        note.tags.forEach { tagName ->
            tagDataSource.linkToNote(note.id, tagName)
        }
        
        note
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        noteDataSource.delete(id)
    }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDataSource.search(query).map { entities ->
            entities.map { entity ->
                val tags = tagDataSource.observeByNote(entity.id).first().map { it.name }
                entity.toDomain(tags)
            }
        }
}
