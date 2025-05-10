package com.invictoprojects.marketplace.observer

interface ReviewPublisher {
    fun addSubscriber(sub: ReviewSubscriber)
    fun removeSubscriber(sub: ReviewSubscriber)
    fun notify(productId: Long, newRating: Int?, oldRating: Int?)
}