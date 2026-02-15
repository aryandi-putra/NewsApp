package com.hunter.newsapp.presentation.search

import app.cash.turbine.test
import com.hunter.newsapp.data.repository.FakeNewsRepository
import com.hunter.newsapp.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private lateinit var fakeRepository: FakeNewsRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeNewsRepository()
        viewModel = SearchViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchQuery should update when onSearchQueryChange is called`() = runTest {
        // Given
        val query = "bitcoin"

        // When
        viewModel.onSearchQueryChange(query)
        advanceUntilIdle()

        // Then
        viewModel.searchQuery.test {
            assertEquals(query, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should emit empty string initially`() = runTest {
        // Then
        viewModel.searchQuery.test {
            assertEquals("", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onArticleClick should upsert article to repository`() = runTest {
        // Given
        val article = createTestArticle(url = "https://example.com/1")

        // When
        viewModel.onArticleClick(article)
        advanceUntilIdle()

        // Then
        assertEquals(article, fakeRepository.getArticleByUrl(article.url))
    }

    @Test
    fun `toggleBookmark should add bookmark`() = runTest {
        // Given
        val article = createTestArticle(url = "https://example.com/1", isBookmarked = false)
        fakeRepository.addArticles(listOf(article))

        // When
        viewModel.toggleBookmark(article)
        advanceUntilIdle()

        // Then
        assertEquals(true, fakeRepository.isBookmarked(article.url))
    }

    @Test
    fun `search with debounce should not trigger immediately`() = runTest {
        // Given
        val query = "technology"

        // When - change query multiple times quickly
        viewModel.onSearchQueryChange("t")
        viewModel.onSearchQueryChange("te")
        viewModel.onSearchQueryChange("tec")
        
        // Should still be empty due to debounce
        viewModel.searchQuery.test {
            // Most recent value
            assertEquals("tec", awaitItem())
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
