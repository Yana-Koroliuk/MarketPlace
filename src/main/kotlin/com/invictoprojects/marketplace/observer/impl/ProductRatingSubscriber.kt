package com.invictoprojects.marketplace.observer.impl

import com.invictoprojects.marketplace.observer.ReviewPublisher
import com.invictoprojects.marketplace.observer.ReviewSubscriber
import com.invictoprojects.marketplace.service.ProductService
import org.springframework.stereotype.Component

@Component
class ProductRatingSubscriber(
    private val productService: ProductService,
    publisher: ReviewPublisher
) : ReviewSubscriber {

    init { publisher.addSubscriber(this) }

    override fun onReviewChanged(productId: Long, newRating: Int?, oldRating: Int?) {
        val product = productService.findById(productId)
        productService.updateAvgRating(product, newRating, oldRating)
    }
}
