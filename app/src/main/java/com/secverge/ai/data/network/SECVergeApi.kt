package com.secverge.ai.data.network

import com.secverge.ai.data.models.ask.AskRequest
import com.secverge.ai.data.models.ask.AskResponse
import com.secverge.ai.data.models.auth.LoginRequest
import com.secverge.ai.data.models.auth.LoginResponse
import com.secverge.ai.data.models.auth.MeResponse
import com.secverge.ai.data.models.cockpit.AdapterStatusResponse
import com.secverge.ai.data.models.cockpit.ContextSnapshotRequest
import com.secverge.ai.data.models.cockpit.ContextSnapshotResponse
import com.secverge.ai.data.models.cockpit.ExecuteRequest
import com.secverge.ai.data.models.cockpit.ExecuteResponse
import com.secverge.ai.data.models.cockpit.RouteRequest
import com.secverge.ai.data.models.cockpit.RouteResponse
import com.secverge.ai.data.models.cockpit.SwitchRequest
import com.secverge.ai.data.models.cockpit.SwitchResponse
import com.secverge.ai.data.models.council.CouncilCreateRequest
import com.secverge.ai.data.models.council.CouncilCreateResponse
import com.secverge.ai.data.models.council.CouncilDebateRequest
import com.secverge.ai.data.models.council.CouncilDebateResponse
import com.secverge.ai.data.models.council.CouncilFinalizeRequest
import com.secverge.ai.data.models.council.CouncilFinalizeResponse
import com.secverge.ai.data.models.delphi.DelphiBlindSubmitRequest
import com.secverge.ai.data.models.delphi.DelphiBlindSubmitResponse
import com.secverge.ai.data.models.delphi.DelphiConvergeRequest
import com.secverge.ai.data.models.delphi.DelphiConvergeResponse
import com.secverge.ai.data.models.delphi.DelphiCreateRequest
import com.secverge.ai.data.models.delphi.DelphiCreateResponse
import com.secverge.ai.data.models.delphi.DelphiDebateRequest
import com.secverge.ai.data.models.delphi.DelphiDebateResponse
import com.secverge.ai.data.models.humancouncil.HumanCouncilContributeRequest
import com.secverge.ai.data.models.humancouncil.HumanCouncilContributeResponse
import com.secverge.ai.data.models.humancouncil.HumanCouncilCreateRequest
import com.secverge.ai.data.models.humancouncil.HumanCouncilCreateResponse
import com.secverge.ai.data.models.humancouncil.HumanCouncilDirectRequest
import com.secverge.ai.data.models.humancouncil.HumanCouncilDirectResponse
import com.secverge.ai.data.models.cockpit.ProviderStreamRequest
import okhttp3.ResponseBody
import retrofit2.http.Headers
import retrofit2.http.Streaming
import com.secverge.ai.data.models.council.CouncilPerspectiveRequest
import com.secverge.ai.data.models.council.CouncilConsensusRequest
import com.secverge.ai.data.models.council.CouncilProviderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SECVergeApi {
    @POST("/api/v1/portal/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/api/v1/portal/me")
    suspend fun me(): MeResponse

    @POST("/api/v1/ask")
    suspend fun ask(@Body request: AskRequest): AskResponse

    @POST("/api/v1/multillm/context/snapshot")
    suspend fun contextSnapshot(@Body request: ContextSnapshotRequest): ContextSnapshotResponse

    @POST("/api/v1/multillm/route")
    suspend fun route(@Body request: RouteRequest): RouteResponse

    @POST("/api/v1/multillm/switch")
    suspend fun switchModel(@Body request: SwitchRequest): SwitchResponse

    @Streaming
    @Headers("Accept: text/event-stream")
    @POST("/api/v1/multillm/stream")
    suspend fun streamModel(@Body request: ProviderStreamRequest): ResponseBody

    @POST("/api/v1/multillm/execute")
    suspend fun execute(@Body request: ExecuteRequest): ExecuteResponse

    @GET("/api/v1/multillm/adapter-status")
    suspend fun adapterStatus(): AdapterStatusResponse

    @POST("/api/v1/ai-council/provider-r4-perspective")
    suspend fun councilPerspective(@Body request: CouncilPerspectiveRequest): CouncilProviderResponse

    @POST("/api/v1/ai-council/provider-r4-consensus")
    suspend fun councilConsensus(@Body request: CouncilConsensusRequest): CouncilProviderResponse

    @POST("/api/v1/council/create")
    suspend fun createCouncil(@Body request: CouncilCreateRequest): CouncilCreateResponse

    @POST("/api/v1/council/debate")
    suspend fun debateCouncil(@Body request: CouncilDebateRequest): CouncilDebateResponse

    @POST("/api/v1/council/finalize")
    suspend fun finalizeCouncil(@Body request: CouncilFinalizeRequest): CouncilFinalizeResponse

    @POST("/api/v1/human-council/create")
    suspend fun createHumanCouncil(@Body request: HumanCouncilCreateRequest): HumanCouncilCreateResponse

    @POST("/api/v1/human-council/contribute")
    suspend fun contributeHumanCouncil(@Body request: HumanCouncilContributeRequest): HumanCouncilContributeResponse

    @POST("/api/v1/human-council/direct")
    suspend fun directHumanCouncil(@Body request: HumanCouncilDirectRequest): HumanCouncilDirectResponse

    @POST("/api/v1/delphi/create")
    suspend fun createDelphi(@Body request: DelphiCreateRequest): DelphiCreateResponse

    @POST("/api/v1/delphi/blind-submit")
    suspend fun blindSubmitDelphi(@Body request: DelphiBlindSubmitRequest): DelphiBlindSubmitResponse

    @POST("/api/v1/delphi/debate")
    suspend fun debateDelphi(@Body request: DelphiDebateRequest): DelphiDebateResponse

    @POST("/api/v1/delphi/converge")
    suspend fun convergeDelphi(@Body request: DelphiConvergeRequest): DelphiConvergeResponse
}
