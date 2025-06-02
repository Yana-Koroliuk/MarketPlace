package com.invictoprojects.marketplace.service.impl

import com.invictoprojects.marketplace.persistence.model.Category
import com.invictoprojects.marketplace.persistence.model.Product
import com.invictoprojects.marketplace.persistence.model.User
import com.invictoprojects.marketplace.service.ProductService
import com.invictoprojects.marketplace.service.UserService
import com.invictoprojects.marketplace.strategy.RecommendationStrategy
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
internal class RecommendationServiceImplTest {

    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var productService: ProductService

    @MockK
    lateinit var randomStrategy: RecommendationStrategy

    @MockK
    lateinit var topStrategy: RecommendationStrategy

    private lateinit var recommendationService: RecommendationServiceImpl

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var pool: List<Product>
    private lateinit var recRandom: List<Product>
    private lateinit var recTop: List<Product>

    @BeforeEach
    fun setUp() {
        user1 = User(username = "u1", email = "u1@mail.com", passwordHash = "hash")
        user2 = User(username = "u2", email = "u2@mail.com", passwordHash = "hash")

        val defCat = Category("def")
        pool = listOf(
            Product(id = 1, name = "p1", price = BigDecimal.TEN, quantity = 1, category = defCat),
            Product(id = 2, name = "p2", price = BigDecimal.ONE, quantity = 1, category = defCat),
            Product(id = 3, name = "p3", price = BigDecimal.ONE, quantity = 1, category = defCat),
            Product(id = 4, name = "p4", price = BigDecimal.ONE, quantity = 1, category = defCat)
        )
        recRandom = pool.take(3)
        recTop = pool.reversed().take(3)

        every { productService.findAll() } returns pool.toMutableList()
        every { userService.findAllBySubscribedIsTrue() } returns mutableListOf(user1, user2)
        every { randomStrategy.recommend(any(), any(), 3) } returns recRandom
        every { topStrategy.recommend(any(), any(), 3) } returns recTop

        val strategies = mapOf(
            "randomStrategy" to randomStrategy,
            "topRatedStrategy" to topStrategy
        )
        recommendationService = RecommendationServiceImpl(
            userService = userService,
            productService = productService,
            strategies = strategies,
            defaultStrategyKey = "randomStrategy"
        )
    }

    @Test
    fun getAllUsersWithRecommendedProducts_DefaultStrategy_ReturnsRandomRecommendations() {
        val result = recommendationService.getAllUsersWithRecommendedProducts()

        assertEquals(2, result.size)
        assertEquals(user1, result[0].first)
        assertEquals(recRandom, result[0].second)
        assertEquals(user2, result[1].first)
        assertEquals(recRandom, result[1].second)

        verify(exactly = 2) { randomStrategy.recommend(any(), pool, 3) }
        verify { topStrategy wasNot Called }
    }

    @Test
    fun setStrategyKey_TopRatedStrategy_SwitchesAlgorithm() {
        recommendationService.setStrategyKey("topRatedStrategy")

        val result = recommendationService.getAllUsersWithRecommendedProducts()

        assertTrue(result.all { it.second == recTop })
        verify(exactly = 2) { topStrategy.recommend(any(), pool, 3) }
        verify { randomStrategy wasNot Called }
    }

    @Test
    fun setStrategyKey_UnknownKey_ThrowsIllegalStateException() {
        assertThrows<IllegalStateException> {
            recommendationService.setStrategyKey("unknownStrategy")
        }
    }
}
