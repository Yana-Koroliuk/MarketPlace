package com.invictoprojects.marketplace.strategy.impl

import com.invictoprojects.marketplace.persistence.model.Product
import com.invictoprojects.marketplace.persistence.model.User
import com.invictoprojects.marketplace.strategy.RecommendationStrategy
import org.springframework.stereotype.Component

@Component("cheapestStrategy")
class CheapestRecommendationStrategy : RecommendationStrategy {
    override fun recommend(user: User, pool: List<Product>, limit: Int): List<Product> =
        pool.sortedBy { it.price }
            .take(limit)
}
