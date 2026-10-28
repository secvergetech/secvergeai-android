package com.secverge.ai.ui.cockpit

import com.secverge.ai.data.models.cockpit.ContextSnapshotRequest
import com.secverge.ai.data.models.cockpit.ExecuteResponse
import com.secverge.ai.data.models.cockpit.ExecutionPayload
import com.secverge.ai.data.models.cockpit.ProviderStreamRequest
import com.secverge.ai.data.network.ProviderStreamParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.secverge.ai.data.models.cockpit.RouteRequest
import com.secverge.ai.data.models.cockpit.SwitchRequest
import com.secverge.ai.data.network.SECVergeApi
import com.secverge.ai.ui.askhachy.AskHachyRepository

class CockpitRepository(
    private val api: SECVergeApi
) {
    private val askHachyRepository =
        AskHachyRepository(api = api)

    suspend fun askHachyDirect(
        prompt: String,
        conversationId: String? = null
    ) = askHachyRepository.ask(
        prompt = prompt,
        conversationId = conversationId
    )

    suspend fun adapterStatus() = runCatching {
        api.adapterStatus()
    }

    suspend fun buildContext(
        prompt: String,
        fromModel: String
    ) = runCatching {
        api.contextSnapshot(
            ContextSnapshotRequest(
                conversationId = null,
                prompt = prompt,
                fromModel = fromModel
            )
        )
    }

    suspend fun recommendRoute(
        prompt: String,
        taskType: String,
        privacy: String,
        fromModel: String,
        toModel: String
    ) = runCatching {
        api.route(
            RouteRequest(
                prompt = prompt,
                taskType = taskType,
                privacy = privacy,
                privacyMode = privacy,
                fromModel = fromModel,
                toModel = toModel
            )
        )
    }

    suspend fun switchModel(
        snapshotId: String,
        fromModel: String,
        toModel: String
    ) = runCatching {
        api.switchModel(
            SwitchRequest(
                snapshotId = snapshotId,
                fromModel = fromModel,
                toModel = toModel,
                preserveContext = true
            )
        )
    }

    suspend fun execute(
        switchId: String?,
        prompt: String,
        model: String,
        snapshotId: String? = null,
        routeId: String? = null
    ) = runCatching {
        val started = System.nanoTime()
        val body = api.streamModel(ProviderStreamRequest(prompt, model))
        val answer = withContext(Dispatchers.IO) {
            body.use { ProviderStreamParser.read(it.charStream().buffered(), model) }
        }
        if (com.secverge.ai.BuildConfig.DEBUG) {
            android.util.Log.i("SECVergeModel", "provider_model=${answer.providerModel} status=complete elapsedMs=${(System.nanoTime() - started) / 1_000_000}")
        }
        ExecuteResponse(execution = ExecutionPayload(response = answer.text, selectedModel = answer.providerModel))
    }
}
