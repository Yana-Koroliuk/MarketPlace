package com.invictoprojects.marketplace.strategy.impl

import com.invictoprojects.marketplace.persistence.model.Product
import com.invictoprojects.marketplace.persistence.model.User
import com.invictoprojects.marketplace.strategy.RecommendationStrategy
import org.springframework.stereotype.Component

@Component("topRatedStrategy")
class TopRatedRecommendationStrategy : RecommendationStrategy {

    override fun recommend(user: User, pool: List<Product>, limit: Int): List<Product> =
        pool.sortedByDescending { it.avgRating ?: 0f }
            .take(limit)
}
