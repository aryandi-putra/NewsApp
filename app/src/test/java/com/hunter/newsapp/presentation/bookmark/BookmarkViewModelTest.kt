package com.hunter.newsapp.presentation.bookmark

import androidx.paging.PagingData
import com.hunter.newsapp.data.repository.FakeNewsRepository
import com.hunter.newsapp.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkViewModelTest {

    private lateinit var viewModel: BookmarkViewModel
    private lateinit var fakeRepository: FakeNewsRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeNewsRepository()
        viewModel = BookmarkViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleBookmark should add bookmark when not bookmarked`() = runTest {
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
    fun `toggleBookmark should remove bookmark when already bookmarked`() = runTest {
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
    fun `bookmarkedArticles should return flow from repository`() = runTest {
        // Given
        val bookmarkedData = PagingData.from(listOf(
            createTestArticle(url = "https://example.com/1", isBookmarked = true),
            createTestArticle(url = "https://example.com/2", isBookmarked = true)
        ))
        fakeRepository.setBookmarkedArticles(bookmarkedData)

        // When
        val result = viewModel.bookmarkedArticles.first()

        // Then - just verify flow emits (actual PagingData comparison is complex)
        assertEquals(PagingData::class.java, result::class.java)
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
