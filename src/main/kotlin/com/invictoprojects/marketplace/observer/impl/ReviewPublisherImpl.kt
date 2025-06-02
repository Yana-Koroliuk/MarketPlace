package com.invictoprojects.marketplace.observer.impl

import com.invictoprojects.marketplace.observer.ReviewPublisher
import com.invictoprojects.marketplace.observer.ReviewSubscriber
import org.springframework.stereotype.Component
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

@Component
class ReviewPublisherImpl : ReviewPublisher {

    private val subs = Collections.newSetFromMap(
        ConcurrentHashMap<ReviewSubscriber, Boolean>()
    )

    override fun addSubscriber(sub: ReviewSubscriber)  { subs += sub }
    override fun removeSubscriber(sub: ReviewSubscriber) { subs -= sub }

    override fun notify(productId: Long, newRating: Int?, oldRating: Int?) {
        subs.forEach { subscriber -> subscriber.onReviewChanged(productId, newRating, oldRating) }
    }
}