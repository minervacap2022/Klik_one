package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryFeedbackDataSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.Feedback
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackData
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackElementType
import io.github.fletchmckee.liquid.samples.app.domain.repository.FeedbackRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.FeedbackStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of FeedbackRepository.
 * PRODUCTION: Requires InMemoryFeedbackDataSource - no optional dependencies.
 */
class FeedbackRepositoryImpl(
    private val dataSource: InMemoryFeedbackDataSource
) : FeedbackRepository {

    private val _feedbackFlow = MutableStateFlow<Result<List<Feedback>>>(Result.Loading)

    init {
        refreshFeedbackInternal()
    }

    private fun refreshFeedbackInternal() {
        try {
            val feedback = dataSource.getFeedback()
            _feedbackFlow.value = Result.Success(feedback)
        } catch (e: Exception) {
            _feedbackFlow.value = Result.Error(e, "Failed to load feedback")
        }
    }

    override fun getFeedbackFlow(): Flow<Result<List<Feedback>>> = _feedbackFlow

    override suspend fun getFeedback(): Result<List<Feedback>> {
        return try {
            val feedback = dataSource.getFeedback()
            Result.Success(feedback)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get feedback")
        }
    }

    override suspend fun getFeedbackById(id: String): Result<Feedback> {
        return try {
            val feedback = dataSource.getFeedbackById(id)
                ?: throw NoSuchElementException("Feedback not found: $id")
            Result.Success(feedback)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get feedback")
        }
    }

    override suspend fun getFeedbackForElement(
        elementType: FeedbackElementType,
        elementId: String
    ): Result<List<Feedback>> {
        return try {
            val feedback = dataSource.getFeedbackForElement(elementType, elementId)
            Result.Success(feedback)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get feedback for element")
        }
    }

    override suspend fun getFeedbackForScreen(screen: String): Result<List<Feedback>> {
        return try {
            val feedback = dataSource.getFeedbackForScreen(screen)
            Result.Success(feedback)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get feedback for screen")
        }
    }

    override suspend fun submitFeedback(
        elementType: FeedbackElementType,
        elementId: String,
        feedbackData: FeedbackData,
        screen: String
    ): Result<Feedback> {
        return try {
            val feedback = dataSource.submitFeedback(elementType, elementId, feedbackData, screen)
                ?: throw IllegalStateException("Failed to submit feedback")
            refreshFeedbackInternal()
            Result.Success(feedback)
        } catch (e: Exception) {
            Result.Error(e, "Failed to submit feedback")
        }
    }

    override suspend fun updateFeedback(feedback: Feedback): Result<Feedback> {
        return try {
            val updated = dataSource.updateFeedback(feedback)
                ?: throw NoSuchElementException("Feedback not found: ${feedback.id}")
            refreshFeedbackInternal()
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update feedback")
        }
    }

    override suspend fun deleteFeedback(feedbackId: String): Result<Unit> {
        return try {
            val success = dataSource.deleteFeedback(feedbackId)
            if (!success) {
                throw NoSuchElementException("Feedback not found: $feedbackId")
            }
            refreshFeedbackInternal()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete feedback")
        }
    }

    override suspend fun getPendingFeedback(): Result<List<Feedback>> {
        return try {
            val feedback = dataSource.getPendingFeedback()
            Result.Success(feedback)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get pending feedback")
        }
    }

    override suspend fun syncFeedback(): Result<Int> {
        return try {
            val count = dataSource.syncFeedback()
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(e, "Failed to sync feedback")
        }
    }

    override suspend fun getFeedbackStats(): Result<FeedbackStats> {
        return try {
            val stats = dataSource.getFeedbackStats()
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get feedback stats")
        }
    }
}
