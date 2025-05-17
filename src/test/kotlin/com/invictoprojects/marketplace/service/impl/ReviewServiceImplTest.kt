package com.invictoprojects.marketplace.service.impl

import com.invictoprojects.marketplace.observer.ReviewPublisher
import com.invictoprojects.marketplace.persistence.model.Category
import com.invictoprojects.marketplace.persistence.model.Product
import com.invictoprojects.marketplace.persistence.model.Review
import com.invictoprojects.marketplace.persistence.model.ReviewId
import com.invictoprojects.marketplace.persistence.model.User
import com.invictoprojects.marketplace.persistence.repository.ReviewRepository
import io.mockk.Called
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.EntityNotFoundException

@ExtendWith(MockKExtension::class)
internal class ReviewServiceImplTest {

    @MockK
    lateinit var reviewRepository: ReviewRepository

    @MockK(relaxed = true)
    lateinit var publisher: ReviewPublisher

    @InjectMockKs
    private lateinit var reviewService: ReviewServiceImpl

    private fun stubReview() = Review(
        author = User(id = 1, username = "author", email = "author@gmail.com"),
        product = Product(
            id = 1,
            name = "product",
            description = "description",
            category = Category("category"),
            price = BigDecimal.valueOf(100),
            quantity = 100
        ),
        rating = 5,
        content = "content",
        date = Instant.EPOCH
    )

    @Test
    fun create_NewReview_CallsSaveAndNotifies() {
        val review = stubReview()
        val reviewId = ReviewId(review.author!!.id, review.product!!.id)

        every { reviewRepository.existsById(reviewId) } returns false
        every { reviewRepository.save(review) } returns review

        val result = reviewService.create(review)

        verifyOrder {
            reviewRepository.existsById(reviewId)
            reviewRepository.save(review)
            publisher.notify(review.product!!.id!!, review.rating, null)
        }

        assertNotNull(result.date)
        assertEquals(review, result)
    }

    @Test
    fun create_ReviewAlreadyExists_ThrowsIllegalArgumentException() {
        val review = stubReview()
        val reviewId = ReviewId(review.author!!.id, review.product!!.id)

        every { reviewRepository.existsById(reviewId) } returns true

        assertThrows<IllegalArgumentException> { reviewService.create(review) }

        verify { reviewRepository.existsById(reviewId) }
        verify { reviewRepository.save(any()) wasNot Called }
        verify { publisher wasNot Called }
    }

    @Test
    fun update_ExistingReview_CallsSaveAndNotifies() {
        val review = stubReview()
        val reviewId = ReviewId(review.author!!.id, review.product!!.id)
        val oldReview = Review(
            author = review.author,
            product = review.product,
            rating = null,
            content = review.content,
            date = review.date
        )

        every { reviewRepository.findById(reviewId) } returns java.util.Optional.of(oldReview)
        every { reviewRepository.save(any()) } returns review

        val result = reviewService.update(review)

        verifyOrder {
            reviewRepository.findById(reviewId)
            reviewRepository.save(review)
            publisher.notify(review.product!!.id!!, review.rating, oldReview.rating)
        }
        assertEquals(review, result)
    }

    @Test
    fun delete_ExistingReview_CallsDeleteAndNotifies() {
        val review = stubReview()
        val reviewId = ReviewId(review.author!!.id, review.product!!.id)

        every { reviewRepository.existsById(reviewId) } returns true
        every { reviewRepository.delete(review) } just Runs

        reviewService.delete(review)

        verifyOrder {
            reviewRepository.existsById(reviewId)
            reviewRepository.delete(review)
            publisher.notify(review.product!!.id!!, null, review.rating)
        }
    }

    @Test
    fun findById_ReviewExists_ReturnsReview() {
        val review = stubReview()
        val reviewId = ReviewId(review.author!!.id, review.product!!.id)

        every { reviewRepository.findById(reviewId) } returns java.util.Optional.of(review)

        val result = reviewService.findById(review.author!!.id!!, review.product!!.id!!)

        verify { reviewRepository.findById(reviewId) }
        assertEquals(review, result)
    }

    @Test
    fun findById_ReviewNotFound_ThrowsEntityNotFoundException() {
        val review = stubReview()
        val reviewId = ReviewId(review.author!!.id, review.product!!.id)

        every { reviewRepository.findById(reviewId) } returns java.util.Optional.empty()

        assertThrows<EntityNotFoundException> {
            reviewService.findById(review.author!!.id!!, review.product!!.id!!)
        }

        verify { reviewRepository.findById(reviewId) }
    }
}
