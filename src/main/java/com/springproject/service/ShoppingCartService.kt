package com.springproject.service

import com.springproject.entity.OrderEntity
import com.springproject.entity.ProductEntity
import com.springproject.exceptions.IdNotFoundException
import com.springproject.model.OrderPositionResponse
import com.springproject.model.ShoppingCardResponse
import com.springproject.repository.OrderPositionRepository
import com.springproject.repository.OrderRepository
import com.springproject.repository.ProductRepository
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class ShoppingCartService (
    val orderRepository: OrderRepository,
    val orderPositionRepository: OrderPositionRepository,
    val productRepository: ProductRepository
        ){

    fun getShoppingCardForCustomer(customerId: String) : ShoppingCardResponse {

        val orders : List<OrderEntity> = orderRepository.findAllByCustomerIdWhereOrderStatusIsNew(customerId)
        val orderIds = orders.map { it.id }
        val orderPositions = orderPositionRepository.findAllById(orderIds).map { OrderService.mapToResponse(it) }
        val deliveryCost = 800L // TODO feature to selecet delivery method??
        val totalAmount = getTotalCart(orderPositions, deliveryCost)


        return ShoppingCardResponse(
            customerId = customerId,
            orderPositions = orderPositions,
            deliveryOption = "STANDART",
            deliveryCostInCent = deliveryCost,
            totalAmountInCent = totalAmount
        )
    }

    fun getTotalCart(orderPositions: List<OrderPositionResponse>, deliveryCost: Long): Long {
        val positionAmounts: List<Int> = orderPositions.map {
            val product: ProductEntity = productRepository
                .findById(it.productId)
                .orElseThrow { throw IdNotFoundException("product with the id ${it.productId} not found") }
            if (it.quantity <= 0)
                throw IllegalArgumentException("OrderPosition with quantity of ${it.quantity} is not allowed.")
            it.quantity * product.priceInCent
        }
        return positionAmounts.sum() + deliveryCost
    }
}
