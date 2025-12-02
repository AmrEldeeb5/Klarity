package com.example.sentio.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.sentio.db.SentioDatabase
import com.example.sentio.domain.models.Note
import com.example.sentio.domain.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class SqlDelightNoteRepository(
    private val database: SentioDatabase
) : NoteRepository {

    private val queries = database.noteQueries
    private val tagQueries = database.tagQueries

    override fun getAllNotes(): Flow<List<Note>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes -> notes.map { it.toDomainModel() } }
    }

    override suspend fun getNoteById(id: String): Note? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun getNotesByFolder(folderId: String): Flow<List<Note>> {
        return queries.selectByFolder(folderId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes -> notes.map { it.toDomainModel() } }
    }

    override fun getNotesByTag(tagId: String): Flow<List<Note>> {
        // This would require a more complex query joining Note and NoteTag tables
        // For now, filter in memory
        return getAllNotes().map { notes ->
            notes.filter { note -> note.tags.contains(tagId) }
        }
    }

    override fun getPinnedNotes(): Flow<List<Note>> {
        return queries.selectPinned()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes -> notes.map { it.toDomainModel() } }
    }

    override fun getFavoriteNotes(): Flow<List<Note>> {
        return queries.selectFavorites()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes -> notes.map { it.toDomainModel() } }
    }

    override suspend fun createNote(note: Note): Result<Note> = withContext(Dispatchers.IO) {
        try {
            queries.insert(
                id = note.id,
                title = note.title,
                content = note.content,
                folderId = note.folderId,
                createdAt = note.createdAt.toEpochMilliseconds(),
                updatedAt = note.updatedAt.toEpochMilliseconds(),
                isPinned = if (note.isPinned) 1 else 0,
                isFavorite = if (note.isFavorite) 1 else 0
            )
            
            // Link tags (tags are now just strings, store them directly)
            note.tags.forEach { tagName ->
                tagQueries.linkNoteTag(note.id, tagName)
            }
            
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Note> = withContext(Dispatchers.IO) {
        try {
            queries.update(
                title = note.title,
                content = note.content,
                folderId = note.folderId,
                updatedAt = note.updatedAt.toEpochMilliseconds(),
                isPinned = if (note.isPinned) 1 else 0,
                isFavorite = if (note.isFavorite) 1 else 0,
                id = note.id
            )
            
            // Update tags (tags are now just strings)
            tagQueries.unlinkAllNoteTags(note.id)
            note.tags.forEach { tagName ->
                tagQueries.linkNoteTag(note.id, tagName)
            }
            
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            queries.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return queries.search(query, query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes -> notes.map { it.toDomainModel() } }
    }

    private suspend fun com.example.sentio.db.Note.toDomainModel(): Note {
        val tags = tagQueries.selectByNote(id).executeAsList().map { tag ->
            tag.name // Just return the tag name as a string
        }
        
        return Note(
            id = id,
            title = title,
            content = content,
            folderId = folderId,
            tags = tags,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt),
            isPinned = isPinned == 1L,
            isFavorite = isFavorite == 1L
        )
    }
}
