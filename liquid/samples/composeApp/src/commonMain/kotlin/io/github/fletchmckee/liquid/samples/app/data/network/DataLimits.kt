package io.github.fletchmckee.liquid.samples.app.data.network

/**
 * Configurable data limits for API requests and UI display.
 * These limits can be adjusted based on performance requirements and user experience.
 */
object DataLimits {
    /**
     * API Request Limits - Control how much data is fetched from the backend
     */
    object API {
        /**
         * Default limit for list queries when no limit is specified.
         */
        var defaultQueryLimit: Int = 100

        /**
         * Maximum number of items to fetch in a single query.
         */
        var maxQueryLimit: Int = 1000

        /**
         * Default page size for paginated requests.
         */
        var defaultPageSize: Int = 20
    }

    /**
     * UI Display Limits - Control how much data is shown in the UI
     */
    object UI {
        /**
         * Maximum number of items to display in lists.
         * Used in WorkLifeScreen and other list views.
         */
        var maxListItems: Int = 50

        /**
         * Maximum length for preview text (e.g., in FeedbackPopup).
         */
        var maxPreviewLength: Int = 200

        /**
         * Maximum length for titles in compact views.
         */
        var maxTitleLength: Int = 100

        /**
         * Maximum number of sources to display in AskKlik chat.
         */
        var maxChatSources: Int = 5
    }

    /**
     * Data Processing Limits - Control how data is processed
     */
    object Processing {
        /**
         * Maximum length for DTO subtitle fields before truncation.
         */
        var maxSubtitleLength: Int = 150
    }

    /**
     * Reset all limits to their default values.
     */
    fun resetToDefaults() {
        API.defaultQueryLimit = 100
        API.maxQueryLimit = 1000
        API.defaultPageSize = 20

        UI.maxListItems = 50
        UI.maxPreviewLength = 200
        UI.maxTitleLength = 100
        UI.maxChatSources = 5

        Processing.maxSubtitleLength = 150
    }

    /**
     * Configure for low-memory devices.
     */
    fun configureLowMemory() {
        API.defaultQueryLimit = 50
        API.maxQueryLimit = 200
        API.defaultPageSize = 10

        UI.maxListItems = 25
        UI.maxPreviewLength = 100
        UI.maxTitleLength = 50
        UI.maxChatSources = 3

        Processing.maxSubtitleLength = 100
    }

    /**
     * Configure for high-performance devices.
     */
    fun configureHighPerformance() {
        API.defaultQueryLimit = 200
        API.maxQueryLimit = 2000
        API.defaultPageSize = 50

        UI.maxListItems = 100
        UI.maxPreviewLength = 400
        UI.maxTitleLength = 200
        UI.maxChatSources = 10

        Processing.maxSubtitleLength = 300
    }
}
