package io.github.fletchmckee.liquid.samples.app.logging

/**
 * Log severity levels compatible with Elastic Common Schema (ECS).
 * Ordered by severity: DEBUG < INFO < WARN < ERROR
 */
enum class LogLevel(val value: Int, val label: String) {
    DEBUG(0, "DEBUG"),
    INFO(1, "INFO"),
    WARN(2, "WARN"),
    ERROR(3, "ERROR");
}
