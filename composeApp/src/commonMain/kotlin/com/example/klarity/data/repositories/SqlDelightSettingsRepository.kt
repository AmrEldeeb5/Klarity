package com.example.klarity.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.klarity.data.util.DispatcherProvider
import com.example.klarity.db.KlarityDatabase
import com.example.klarity.domain.repositories.AiProvider
import com.example.klarity.domain.repositories.AiSettings
import com.example.klarity.domain.repositories.DEFAULT_AI_TEMPERATURE
import com.example.klarity.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val KEY_PROVIDER = "ai_provider"
private const val KEY_API = "ai_api_key"
private const val KEY_MODEL = "ai_model"
private const val KEY_BASE_URL = "ai_base_url"
private const val KEY_TEMPERATURE = "ai_temperature"
private const val KEY_ACTIONS = "ai_actions_enabled"

/** Persisted marker for "Auto" temperature — kept distinct from an unset value (which defaults). */
private const val TEMPERATURE_AUTO = "auto"

/**
 * Settings repository backed by the [AppSetting] key/value table. The table is ensured to exist at
 * database construction (see the DI module) so this works on databases created before it existed.
 */
class SqlDelightSettingsRepository(
    private val database: KlarityDatabase,
    private val dispatchers: DispatcherProvider,
) : SettingsRepository {

    private val queries get() = database.settingsQueries

    override fun settings(): Flow<AiSettings> =
        queries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows -> rows.associate { it.key to it.value_ }.toSettings() }
            .catch { e ->
                println("Error loading settings: ${e.message}")
                emit(AiSettings())
            }

    override suspend fun current(): AiSettings = withContext(dispatchers.io) {
        queries.selectAll().executeAsList().associate { it.key to it.value_ }.toSettings()
    }

    override suspend fun save(
        provider: AiProvider,
        apiKey: String?,
        model: String,
        baseUrl: String,
        temperature: Double?,
        actionsEnabled: Boolean,
    ): Unit = withContext(dispatchers.io) {
        queries.transaction {
            queries.upsert(KEY_PROVIDER, provider.name)
            val key = apiKey?.trim()
            if (key.isNullOrBlank()) queries.deleteByKey(KEY_API) else queries.upsert(KEY_API, key)
            queries.upsert(KEY_MODEL, model.trim().ifBlank { provider.defaultModel })
            queries.upsert(KEY_BASE_URL, baseUrl.trim().ifBlank { provider.defaultBaseUrl })
            queries.upsert(KEY_TEMPERATURE, temperature?.toString() ?: TEMPERATURE_AUTO)
            queries.upsert(KEY_ACTIONS, actionsEnabled.toString())
        }
    }

    private fun Map<String, String>.toSettings(): AiSettings {
        val provider = AiProvider.fromId(this[KEY_PROVIDER])
        return AiSettings(
            provider = provider,
            apiKey = this[KEY_API]?.takeIf { it.isNotBlank() },
            model = this[KEY_MODEL]?.takeIf { it.isNotBlank() } ?: provider.defaultModel,
            baseUrl = this[KEY_BASE_URL]?.takeIf { it.isNotBlank() } ?: provider.defaultBaseUrl,
            temperature = this[KEY_TEMPERATURE].toTemperature(),
            // Default on; only an explicit "false" disables actions.
            actionsEnabled = this[KEY_ACTIONS]?.toBooleanStrictOrNull() ?: true,
        )
    }

    /** "auto" → null (omit at request time); a number → that value; absent/garbage → the default. */
    private fun String?.toTemperature(): Double? = when {
        this == null -> DEFAULT_AI_TEMPERATURE
        equals(TEMPERATURE_AUTO, ignoreCase = true) -> null
        else -> toDoubleOrNull() ?: DEFAULT_AI_TEMPERATURE
    }
}
