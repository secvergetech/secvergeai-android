package com.secverge.ai.ui.cockpit

import com.secverge.ai.data.models.cockpit.ContextSnapshotRequest
import com.secverge.ai.data.models.cockpit.ExecuteRequest
import com.secverge.ai.data.models.cockpit.RouteRequest
import com.secverge.ai.data.models.cockpit.SwitchRequest
import com.secverge.ai.data.network.SECVergeApi

class CockpitRepository(
    private val api: SECVergeApi
) {
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
                toModel = toModel
            )
        )
    }

    suspend fun execute(
        switchId: String?,
        prompt: String,
        model: String
    ) = runCatching {
        api.execute(
            ExecuteRequest(
                switchId = switchId,
                prompt = prompt,
                model = model
            )
        )
    }
}
