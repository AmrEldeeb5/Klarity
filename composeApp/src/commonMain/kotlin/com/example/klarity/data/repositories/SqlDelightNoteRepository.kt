package com.example.klarity.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.benasher44.uuid.uuid4
import com.example.klarity.data.mapper.toDomain
import com.example.klarity.data.mapper.toEntity
import com.example.klarity.data.util.DispatcherProvider
import com.example.klarity.db.KlarityDatabase
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository implementation for Note operations.
 * Directly uses SQLDelight database - no DataSource layer needed for simple cases.
 *
 * Performance: Uses batch tag loading to avoid N+1 query problem.
 */
class SqlDelightNoteRepository(
    private val database: KlarityDatabase,
    private val dispatchers: DispatcherProvider
) : NoteRepository {

    private val noteQueries get() = database.noteQueries
    private val tagQueries get() = database.tagQueries

    override fun getAllNotes(): Flow<List<Note>> =
        noteQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                // Batch load all tags in single query (fixes N+1 problem)
                val allTags = getAllNoteTagsMap()
                entities.map { entity ->
                    entity.toDomain(allTags[entity.id] ?: emptyList())
                }
            }
            .catch { e ->
                // Log error and emit empty list to prevent crash
                println("Error loading notes: ${e.message}")
                emit(emptyList())
            }

    override suspend fun getNoteById(id: String): Note? = withContext(dispatchers.io) {
        val entity = noteQueries.selectById(id).executeAsOneOrNull() ?: return@withContext null
        val tags = getTagsForNote(id)
        entity.toDomain(tags)
    }

    override fun getNotesByFolder(folderId: String): Flow<List<Note>> =
        noteQueries.selectByFolder(folderId)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                val allTags = getAllNoteTagsMap()
                entities.map { entity ->
                    entity.toDomain(allTags[entity.id] ?: emptyList())
                }
            }
            .catch { emit(emptyList()) }

    override fun getNotesByTag(tagId: String): Flow<List<Note>> =
        getAllNotes().map { notes ->
            notes.filter { note -> note.tags.contains(tagId) }
        }

    override fun getPinnedNotes(): Flow<List<Note>> =
        noteQueries.selectPinned()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                val allTags = getAllNoteTagsMap()
                entities.map { entity ->
                    entity.toDomain(allTags[entity.id] ?: emptyList())
                }
            }
            .catch { emit(emptyList()) }

    override fun getFavoriteNotes(): Flow<List<Note>> =
        noteQueries.selectFavorites()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                val allTags = getAllNoteTagsMap()
                entities.map { entity ->
                    entity.toDomain(allTags[entity.id] ?: emptyList())
                }
            }
            .catch { emit(emptyList()) }

    override suspend fun createNote(note: Note): Result<Note> = runCatching {
        withContext(dispatchers.io) {
            val entity = note.toEntity()
            // Note + its tag links are written atomically so a failure can't leave a half-tagged note.
            noteQueries.transaction {
                noteQueries.insert(
                    id = entity.id,
                    title = entity.title,
                    content = entity.content,
                    folderId = entity.folderId,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    isPinned = entity.isPinned,
                    isFavorite = entity.isFavorite,
                    status = entity.status
                )
                note.tags.forEach { tagName ->
                    tagQueries.linkNoteTag(note.id, ensureTagId(tagName))
                }
            }
        }
        note
    }

    override suspend fun updateNote(note: Note): Result<Note> = runCatching {
        withContext(dispatchers.io) {
            val entity = note.toEntity()
            noteQueries.transaction {
                noteQueries.update(
                    title = entity.title,
                    content = entity.content,
                    folderId = entity.folderId,
                    updatedAt = entity.updatedAt,
                    isPinned = entity.isPinned,
                    isFavorite = entity.isFavorite,
                    status = entity.status,
                    id = entity.id
                )
                // Update tags: remove old, re-link new.
                tagQueries.unlinkAllNoteTags(note.id)
                note.tags.forEach { tagName ->
                    tagQueries.linkNoteTag(note.id, ensureTagId(tagName))
                }
            }
        }
        note
    }

    /**
     * Resolve a tag name to a real Tag.id, creating the Tag row if it doesn't exist yet (tag names
     * are unique). Linking by id is what makes the read-side JOIN (NoteTag.tagId = Tag.id) resolve —
     * previously the name was stored as the tagId, so tags silently vanished on round-trip.
     * Must be called inside a transaction.
     */
    private fun ensureTagId(name: String): String =
        tagQueries.selectByName(name).executeAsOneOrNull()?.id
            ?: uuid4().toString().also { tagQueries.insert(id = it, name = name, color = null) }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            noteQueries.delete(id)
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteQueries.search(query, query)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                // Batch load tags for search results
                val allTags = getAllNoteTagsMap()
                entities.map { entity ->
                    entity.toDomain(allTags[entity.id] ?: emptyList())
                }
            }
            .catch { emit(emptyList()) }

    /**
     * Get tags for a single note (used for single note operations)
     */
    private suspend fun getTagsForNote(noteId: String): List<String> =
        withContext(dispatchers.io) {
            tagQueries.selectByNote(noteId)
                .executeAsList()
                .map { it.name }
        }

    /**
     * Batch load all note-tag relationships in a single query.
     * Returns a map of noteId -> list of tag names.
     * This avoids the N+1 query problem when loading many notes.
     */
    private suspend fun getAllNoteTagsMap(): Map<String, List<String>> =
        withContext(dispatchers.io) {
            tagQueries.selectAllNoteTags()
                .executeAsList()
                .groupBy(
                    keySelector = { it.noteId },
                    valueTransform = { it.name }
                )
        }
}
