package com.tradeteam.TradingEngine;

import com.tradeteam.TradingEngine.entities.Exchange;
import com.tradeteam.TradingEngine.entities.Order;
import com.tradeteam.TradingEngine.entities.OrderBook;
import com.tradeteam.TradingEngine.entities.Trade;
import lombok.ToString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderBookTest {

    OrderBook orderBook;
    Order buyOrder1;
    Order buyOrder2;
    Order sellOrder1;
    Order sellOrder2;
    Order sellOrder3;

    @BeforeEach
    public void setUp() {
        orderBook = new OrderBook("AAPL", new Exchange(),
                new ArrayList<Order>());
        buyOrder1 = new Order(1,
                LocalDateTime.of(2023, 3, 28, 12, 30),
                orderBook, 10, 0,
                15.00, Order.OrderType.BUY, true, new ArrayList<Trade>());
        buyOrder2 = new Order(1,
                LocalDateTime.of(2023, 3, 28, 12, 35),
                orderBook, 5, 0,
                16.00, Order.OrderType.BUY, true, new ArrayList<Trade>());
        sellOrder1 = new Order(2,
                LocalDateTime.of(2023, 3, 28, 12, 40),
                orderBook, 10, 0,
                14.00, Order.OrderType.SELL, true, new ArrayList<Trade>());
        sellOrder2 = new Order(2,
                LocalDateTime.of(2023, 3, 28, 12, 45),
                orderBook, 15, 0,
                17.00, Order.OrderType.SELL, true, new ArrayList<Trade>());
        sellOrder3 = new Order(2,
                LocalDateTime.of(2023, 3, 28, 12, 45),
                orderBook, 12, 0,
                11.00, Order.OrderType.SELL, true, new ArrayList<Trade>());
        // buyOrder1: qty 10, price 15
        // buyOrder2: qty 5, price 16
        // sellOrder1: qty 10, price 14
        // sellOrder2: qty 15, price 17
        // sellOrder3: qty 12, price 11
    }

    @Test
    public void addOrderWithNoOtherOrders() {
        orderBook.addOrder(buyOrder1);
        Assert.assertEquals(buyOrder1, orderBook.getOrders().get(0));
    }

    @Test
    public void addOrderWithNonMatchingOrder() {
        orderBook.addOrder(buyOrder1);
        orderBook.addOrder(buyOrder2);
        Assert.assertEquals(buyOrder2, orderBook.getOrders().get(1));
    }

    @Test
    public void addSellOrderWithOneBuyOrder() {
        orderBook.addOrder(buyOrder1);
        orderBook.addOrder(sellOrder1);
        Assert.assertEquals(10, orderBook.getOrders().get(0).getNumberFulfilled());
        Assert.assertEquals(10, orderBook.getOrders().get(1).getNumberFulfilled());
    }

    @Test
    public void addSellOrderWithWorsePriceThanBuyOrders() {
        orderBook.addOrder(buyOrder1);
        orderBook.addOrder(buyOrder2);
        orderBook.addOrder(sellOrder2);
        Assert.assertEquals(0, sellOrder2.getNumberFulfilled());
    }

    @Test
    public void addSellOrderThatSplitsBuyOrders() {
        orderBook.addOrder(buyOrder1);
        orderBook.addOrder(buyOrder2);
        orderBook.addOrder(sellOrder3);
        Assert.assertEquals(5, buyOrder2.getNumberFulfilled());
        Assert.assertEquals(7, buyOrder1.getNumberFulfilled());
        Assert.assertEquals(12, sellOrder3.getNumberFulfilled());
        Assert.assertTrue(buyOrder1.isOrderActive());
        Assert.assertFalse(buyOrder2.isOrderActive());
        Assert.assertFalse(sellOrder3.isOrderActive());
    }

    @Test
    public void twoIdenticalBuyOrdersExceptTimestamp() {
        Order buyOrderClone = new Order(1,
                LocalDateTime.of(2023, 3, 28, 12, 0),
                orderBook, 10, 0,
                15.00, Order.OrderType.BUY, true, new ArrayList<Trade>());
        orderBook.addOrder(buyOrder1);
        orderBook.addOrder(buyOrderClone);
        orderBook.addOrder(sellOrder3);
        Assert.assertEquals(2, buyOrder1.getNumberFulfilled());
        Assert.assertEquals(10, buyOrderClone.getNumberFulfilled());
        Assert.assertEquals(12, sellOrder3.getNumberFulfilled());
    }

    // buyOrder1: qty 10, price 15
    // buyOrder2: qty 5, price 16
    // sellOrder1: qty 10, price 14
    // sellOrder2: qty 15, price 17
    // sellOrder3: qty 12, price 11

}