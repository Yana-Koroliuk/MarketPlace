package com.invictoprojects.marketplace.strategy

import com.invictoprojects.marketplace.persistence.model.Product
import com.invictoprojects.marketplace.persistence.model.User

fun interface RecommendationStrategy {
    fun recommend(user: User, pool: List<Product>, limit: Int): List<Product>
}
