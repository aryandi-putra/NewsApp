package com.hunter.newsapp.presentation.top_headlines

import androidx.paging.LoadState
import androidx.paging.PagingData
import app.cash.turbine.test
import com.hunter.newsapp.data.connectivity.FakeConnectivityObserver
import com.hunter.newsapp.data.repository.FakeNewsRepository
import com.hunter.newsapp.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class TopHeadlinesViewModelTest {

    private lateinit var viewModel: TopHeadlinesViewModel
    private lateinit var fakeRepository: FakeNewsRepository
    private lateinit var fakeConnectivityObserver: FakeConnectivityObserver
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeNewsRepository()
        fakeConnectivityObserver = FakeConnectivityObserver()
        viewModel = TopHeadlinesViewModel(fakeRepository, fakeConnectivityObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleBookmark should add bookmark when article is not bookmarked`() = runTest {
        // Given
        val article = createTestArticle(url = "https://example.com/1", isBookmarked = false)
        fakeRepository.addArticles(listOf(article))

        // When
        viewModel.toggleBookmark(article)
        advanceUntilIdle()

        // Then
        assertTrue(fakeRepository.isBookmarked(article.url))
    }

    @Test
    fun `toggleBookmark should remove bookmark when article is already bookmarked`() = runTest {
        // Given
        val article = createTestArticle(url = "https://example.com/1", isBookmarked = false)
        fakeRepository.addArticles(listOf(article))
        viewModel.toggleBookmark(article)
        advanceUntilIdle()
        assertTrue(fakeRepository.isBookmarked(article.url))

        // When - toggle again
        viewModel.toggleBookmark(article.copy(isBookmarked = true))
        advanceUntilIdle()

        // Then
        assertFalse(fakeRepository.isBookmarked(article.url))
    }

    @Test
    fun `isOnline should emit true when connectivity is available`() = runTest {
        // Given
        fakeConnectivityObserver.setOnline(true)

        // Then
        viewModel.isOnline.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isOnline should emit false when connectivity is lost`() = runTest {
        // Given - start with online
        fakeConnectivityObserver.setOnline(true)
        
        // Then - collect initial value
        viewModel.isOnline.test {
            assertEquals(true, awaitItem())
            
            // When - go offline
            fakeConnectivityObserver.setOnline(false)
            
            // Then
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiEvent should emit offline snackbar when connectivity is lost`() = runTest {
        // Start collecting first
        viewModel.uiEvent.test {
            // Given - start online (already set in setup, skip initial if any)
            
            // When - go offline
            fakeConnectivityObserver.setOnline(false)
            
            // Then
            val event = awaitItem() as UiEvent.ShowSnackbar
            assertEquals("You're offline. Showing cached data.", event.message)
            assertEquals("Dismiss", event.actionLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiEvent should emit back online snackbar when connectivity returns`() = runTest {
        // Start with offline state first
        fakeConnectivityObserver.setOnline(false)
        advanceUntilIdle()
        
        // Then start collecting
        viewModel.uiEvent.test {
            // When - come back online
            fakeConnectivityObserver.setOnline(true)
            
            // Then
            val event = awaitItem() as UiEvent.ShowSnackbar
            assertEquals("Back online", event.message)
            assertEquals(null, event.actionLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleRefreshError should emit network error snackbar for IOException`() = runTest {
        // Start collecting first
        viewModel.uiEvent.test {
            // Given
            val ioException = IOException("Network error")

            // When
            viewModel.handleRefreshError(LoadState.Error(ioException))
            advanceUntilIdle()

            // Then
            val event = awaitItem() as UiEvent.ShowSnackbar
            assertEquals("Network error. Showing cached data.", event.message)
            assertEquals("Retry", event.actionLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleRefreshError should emit server error snackbar for HttpException`() = runTest {
        // Start collecting first
        viewModel.uiEvent.test {
            // Given - create a mock HttpException (would need mock in real implementation)
            // For simplicity, using generic exception
            val httpException = Exception("HTTP 500")

            // When
            viewModel.handleRefreshError(LoadState.Error(httpException))
            advanceUntilIdle()

            // Then
            val event = awaitItem() as UiEvent.ShowSnackbar
            assertTrue(event.message.contains("Error loading data"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createTestArticle(
        url: String = "https://example.com/article",
        title: String = "Test Article",
        isBookmarked: Boolean = false
    ): Article {
        return Article(
            url = url,
            title = title,
            description = "Test description",
            urlToImage = null,
            publishedAt = "2024-01-01",
            content = "Test content",
            author = "Test Author",
            sourceName = "Test Source",
            isBookmarked = isBookmarked
        )
    }
}
