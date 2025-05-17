package com.invictoprojects.marketplace.service.impl

import com.invictoprojects.marketplace.persistence.model.Product
import com.invictoprojects.marketplace.persistence.model.User
import com.invictoprojects.marketplace.service.ProductService
import com.invictoprojects.marketplace.service.RecommendationService
import com.invictoprojects.marketplace.service.UserService
import com.invictoprojects.marketplace.strategy.RecommendationStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecommendationServiceImpl(
    private val userService: UserService,
    private val productService: ProductService,
    private val strategies: Map<String, RecommendationStrategy>,
    @Value("\${marketplace.recommend.strategy:randomStrategy}")
    private val defaultStrategyKey: String
) : RecommendationService {

    companion object {
        private const val DEFAULT_LIMIT = 3
    }

    @Volatile
    private var strategy: RecommendationStrategy = strategies[defaultStrategyKey]
        ?: error("Unknown strategy bean: $defaultStrategyKey")

    override fun setStrategyKey(key: String) {
        strategy = strategies[key] ?: error("Unknown strategy bean: $key")
    }

    @Transactional
    override fun getAllUsersWithRecommendedProducts(): List<Pair<User, List<Product>>> {
        val pool = productService.findAll()
        return userService.findAllBySubscribedIsTrue()
            .map { user -> user to strategy.recommend(user, pool, DEFAULT_LIMIT) }
            .toList()
    }
}
