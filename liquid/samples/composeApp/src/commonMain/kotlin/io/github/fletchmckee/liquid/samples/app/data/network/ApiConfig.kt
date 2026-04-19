package io.github.fletchmckee.liquid.samples.app.data.network

/**
 * API configuration for network requests.
 * Uses Environment configuration to support multiple deployment environments.
 * Configure via Environment.configure() during app initialization.
 */
object ApiConfig {
    const val TIMEOUT_MS = 30_000L
    const val MAX_RETRIES = 3

    // Dynamic URLs based on current environment
    val BASE_URL: String get() = Environment.current().baseUrl
    val AUTH_BASE_URL: String get() = Environment.current().authBaseUrl
    val AUTH_OAUTH_BASE_URL: String get() = Environment.current().oauthBaseUrl
    val INSIGHTS_BASE_URL: String get() = Environment.current().insightsBaseUrl
    val ENCOURAGE_BASE_URL: String get() = Environment.current().encourageBaseUrl
    val WORKLIFE_BASE_URL: String get() = Environment.current().worklifeBaseUrl
    val ASKKLIK_BASE_URL: String get() = Environment.current().askKlikBaseUrl
    val EXEC_BASE_URL: String get() = Environment.current().execBaseUrl
    val ENTITY_FEEDBACK_BASE_URL: String get() = Environment.current().entityFeedbackBaseUrl
    val NOTIFICATIONS_BASE_URL: String get() = Environment.current().notificationsBaseUrl
    val GOAL_BASE_URL: String get() = Environment.current().goalBaseUrl
    val TREE_BASE_URL: String get() = Environment.current().treeBaseUrl
    val SUBSCRIPTION_BASE_URL: String get() = Environment.current().subscriptionBaseUrl
    val COMPLIANCE_BASE_URL: String get() = Environment.current().complianceBaseUrl
    val BUG_REPORT_BASE_URL: String get() = Environment.current().bugReportBaseUrl
    val LOGS_BASE_URL: String get() = Environment.current().logsBaseUrl
    val WEB_BASE_URL: String get() = Environment.current().webBaseUrl
    val PRIVACY_URL: String get() = "${WEB_BASE_URL}/privacy"
    val TERMS_URL: String get() = "${WEB_BASE_URL}/terms"
    val WEB_APP_DOMAIN: String get() = Environment.current().webBaseUrl.removePrefix("https://").removePrefix("http://")
    val AUTH_OAUTH_URL: String get() = "$AUTH_OAUTH_BASE_URL/oauth"

    // API Endpoints
    object Endpoints {
        // Calendar
        const val MEETINGS = "/meetings"
        const val DAILY_BRIEFING = "/calendar/briefing"

        // Insights (KLIK Insights API - runs on port 8336)
        // Base URL already has /api/insights, so endpoint is just /v1/insights
        const val INSIGHTS = "/v1/insights"

        // Encourage (encouraging summary - runs on port 8335)
        // Base URL already has /api/encourage, so endpoint is just /v1/encourage
        const val ENCOURAGE = "/v1/encourage"

        // Worklife (knowledge graph insights - runs on port 8337)
        // Base URL already has /api/worklife, so endpoint is just /v1/worklife
        const val WORKLIFE = "/v1/worklife"

        // Goals & Level (KK_goal API - runs on port 8091)
        // Base URL already has /api/goal, so endpoints start with /analytics/v1/user
        const val GOALS_LIST = "/analytics/v1/user"  // Append /{user_id}/goals
        const val GOAL_GENERATE = "/analytics/v1/user"  // Append /{user_id}/goals/generate
        const val GOAL_DETAIL = "/analytics/v1/user"  // Append /{user_id}/goals/{goal_id}
        const val USER_LEVEL = "/analytics/v1/user"  // Append /{user_id}/level
        const val XP_HISTORY = "/analytics/v1/user"  // Append /{user_id}/xp/history

        // Tasks
        const val TASKS = "/tasks"
        const val TASK_SUMMARY = "/tasks/summary"

        // Audio Recording (Fixed Session)
        const val AUDIO_START_FIXED = "/audio/start_fixed_session"
        const val AUDIO_STREAM = "/audio/stream_opus"
        const val AUDIO_STOP_FIXED = "/audio/stop"  // Append /{user_id}/fixed
        const val AUDIO_TRANSCRIBE = "/audio/transcribe"

        // People
        const val PEOPLE = "/people"
        const val SEARCH_PEOPLE = "/people/search"

        // Projects
        const val PROJECTS = "/projects"

        // Organizations
        const val ORGANIZATIONS = "/organizations"

        // Growth
        const val SCENARIOS = "/scenarios"
        const val GROWTH_HISTORY = "/growth/history"

        // Growth Tree (uses TREE_BASE_URL - port 8415)
        // Base URL already has /api/v1/growth, so endpoint is just /tree
        const val GROWTH_TREE = "/tree"

        // User
        const val USER = "/user"
        const val USER_PREFERENCES = "/user/preferences"
        const val DEVICES = "/user/devices"

        // Chat/AI (legacy endpoints)
        const val CHAT_MESSAGES = "/chat/messages"
        const val CHAT_SEND = "/chat/send"
        const val SUGGESTED_QUESTIONS = "/chat/suggestions"

        // AskKlik RAG Chat (uses ASKKLIK_BASE_URL)
        // Base URL already has /api/chat, so endpoint is just /v1/chat
        const val ASKKLIK_CHAT = "/v1/chat"
        const val ASKKLIK_HEALTH = "/health"

        // Feedback
        const val FEEDBACK = "/feedback"

        // Entity Feedback (uses ENTITY_FEEDBACK_BASE_URL - runs on port 8339)
        // Base URL already has /api/entity-feedback, so endpoint is just /v1/entities
        const val ENTITY_UPDATE = "/v1/entities"

        // OAuth Endpoints (uses AUTH_OAUTH_URL)
        // GET /providers - List available OAuth providers
        const val OAUTH_PROVIDERS = "/providers"
        // GET /credentials - List user's OAuth credentials
        const val OAUTH_CREDENTIALS = "/credentials"

        // KK_exec Todo Endpoints (uses EXEC_BASE_URL)
        // Base URL already has /api/exec, so endpoints start with /v1/todos
        // GET /v1/todos - List all todos for a user across all sessions (user_id from JWT)
        const val USER_TODOS = "/v1/todos"
        // GET /v1/todos/{session_id} - List todos for a session (user-specific via User-Id header)
        const val TODOS = "/v1/todos"
        // POST /v1/todos/{session_id}/execute - Execute todos for a session
        const val EXECUTE_TODOS = "/v1/todos"  // Append /{session_id}/execute
        // POST /v1/todos/{session_id}/todo/{todo_id}/approve - Approve sensitive todo
        // POST /v1/todos/{session_id}/todo/{todo_id}/reject - Reject sensitive todo
        const val TODO_REVIEW = "/v1/todos"  // Append /{session_id}/todo/{todo_id}/approve or /reject
        // POST /v1/todos/{session_id}/todos/bulk-review - Batch approve/reject
        const val TODO_BULK_REVIEW = "/v1/todos"  // Append /{session_id}/todos/bulk-review
        // GET /v1/todos/pending-review - List all user's in_review todos
        const val TODO_PENDING_REVIEW = "/v1/todos/pending-review"
        // POST /v1/todos/{session_id}/todo/{todo_id}/retry - Retry completed/failed todo
        const val TODO_RETRY = "/v1/todos"  // Append /{session_id}/todo/{todo_id}/retry

        // KK_notifications Endpoints (uses NOTIFICATIONS_BASE_URL)
        // POST /v1/devices/register - Register device token
        const val REGISTER_DEVICE = "/v1/devices/register"
        // DELETE /v1/devices/{token} - Unregister device token
        const val UNREGISTER_DEVICE = "/v1/devices"
        // GET /v1/devices - List user's device tokens
        // GET /v1/notifications - List notifications for user (paginated)
        const val NOTIFICATIONS = "/v1/notifications"
        // POST /v1/notifications/{id}/read - Mark notification as read
        // GET /v1/notifications/unread-count - Get unread notification count
        const val UNREAD_COUNT = "/v1/notifications/unread-count"

        // Subscription (KK_subscription API - port 8416)
        // Base URL already has /api/subscription, so endpoints are relative
        const val SUBSCRIPTION_PLANS = "/plans"          // GET - list tiers (no auth)
        const val SUBSCRIPTION_ME = "/me"                // GET - current plan + usage
        const val SUBSCRIPTION_UPGRADE = "/upgrade"      // POST - upgrade plan
        const val SUBSCRIPTION_DOWNGRADE = "/downgrade"  // POST - downgrade plan
        const val SUBSCRIPTION_USAGE = "/usage"          // GET - detailed usage breakdown
        const val SUBSCRIPTION_FEATURES = "/features"    // GET - feature flags for tier

        // Consent endpoints (uses COMPLIANCE_BASE_URL → /api/compliance/*)
        const val CONSENT_SUBMIT = "/consent"                          // POST {consent_type, consent_version}
        const val CONSENT_STATUS = "/consent"                          // GET /consent/{consent_type}
        const val CONSENT_REVOKE = "/consent"                          // DELETE /consent/{consent_type}
        // Consent types used in request bodies
        const val CONSENT_TYPE_RECORDING = "recording_tos"
        const val CONSENT_TYPE_RECORDING_ALL_PARTY = "recording_all_party"
        const val CONSENT_TYPE_BIOMETRIC = "biometric_voiceprint"

        // Voiceprint endpoints (uses BASE_URL → /api/v1/voiceprint/*)
        const val VOICEPRINTS = "/voiceprint/user"                     // GET list, Append /{user_id}
        const val VOICEPRINT_DELETE = "/voiceprint"                    // DELETE, Append /{voiceprint_id}

        // Privacy settings endpoints (uses COMPLIANCE_BASE_URL → /api/compliance/*)
        const val PRIVACY_SETTINGS = "/privacy-settings"               // GET/PUT unified settings
        const val PRIVACY_DATA_EXPORT = "/export"                      // POST request data export
        const val PRIVACY_REQUEST = "/privacy-request"                 // POST privacy rights request
        const val PRIVACY_REQUESTS = "/privacy-requests"               // GET list requests

        // Account Security (uses AUTH_BASE_URL)
        const val CHANGE_PASSWORD = "/change-password"                  // POST

        // Notification Preferences (uses AUTH_BASE_URL)
        const val NOTIFICATION_PREFERENCES = "/user/notification-preferences" // GET/PUT
    }

    // Header keys
    object Headers {
        const val AUTHORIZATION = "Authorization"
        const val CONTENT_TYPE = "Content-Type"
        const val ACCEPT = "Accept"
        const val API_VERSION = "X-API-Version"
        const val DEVICE_ID = "X-Device-ID"
        const val PLATFORM = "X-Platform"
        const val USER_ID = "User-Id"  // Required by backend API
        const val RECORDING_MODE = "X-Recording-Mode"
    }

    // Content types
    object ContentTypes {
        const val JSON = "application/json"
    }

    // OAuth URL builders
    object OAuth {
        // GET /{provider}/authorize - Get authorization URL
        fun authorizeUrl(provider: String) = "$AUTH_OAUTH_URL/$provider/authorize"
        // GET /{provider}/status - Check connection status
        fun statusUrl(provider: String) = "$AUTH_OAUTH_URL/$provider/status"
        // DELETE /{provider}/disconnect - Remove OAuth credential
        fun disconnectUrl(provider: String) = "$AUTH_OAUTH_URL/$provider/disconnect"
        // Full providers URL
        val providersUrl = "$AUTH_OAUTH_URL${Endpoints.OAUTH_PROVIDERS}"
        // Full credentials URL
        val credentialsUrl = "$AUTH_OAUTH_URL${Endpoints.OAUTH_CREDENTIALS}"
        // POST /native/sync - Sync native integration permission status to backend
        // Used for Apple Calendar/Reminders on iOS
        val nativeSyncUrl = "$AUTH_OAUTH_URL/native/sync"
    }
}

/**
 * API response wrapper for standardized responses
 */
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(
        val code: Int,
        val message: String,
        val details: Map<String, String>? = null
    ) : ApiResponse<Nothing>()
}

/**
 * Pagination wrapper for list responses
 */
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)
