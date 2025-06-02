package com.invictoprojects.marketplace.observer

fun interface ReviewSubscriber {
    fun onReviewChanged(productId: Long, newRating: Int?, oldRating: Int?)
}
