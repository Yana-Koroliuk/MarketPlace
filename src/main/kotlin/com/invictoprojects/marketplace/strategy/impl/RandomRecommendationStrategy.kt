package com.invictoprojects.marketplace.strategy.impl

import com.invictoprojects.marketplace.persistence.model.Product
import com.invictoprojects.marketplace.persistence.model.User
import com.invictoprojects.marketplace.strategy.RecommendationStrategy
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component("randomStrategy")
class RandomRecommendationStrategy : RecommendationStrategy {

    override fun recommend(user: User, pool: List<Product>, limit: Int): List<Product> =
        List(limit.coerceAtMost(pool.size)) { pool[Random.nextInt(pool.size)] }
}
