package com.tradeteam.TradingEngine;

import com.tradeteam.TradingEngine.entities.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class TotalDailyValueTradedTest {

    Exchange exchange1;
    OrderBook orderBook1;
    Order buyOrder1;
    Order buyOrder2;
    Order sellOrder1;
    Order sellOrder2;
    Order sellOrder3;

    @BeforeEach
    public void setUp() {
        exchange1 = new Exchange("NYSE", new ArrayList<>());
        orderBook1 = new OrderBook(new OrderBookId("NYSE", "DB"),
                "Deutsche Bank", exchange1,
                new ArrayList<Order>());
        exchange1.setOrderBooks(Arrays.asList(orderBook1));
        buyOrder1 = new Order(1, 1,
                LocalDateTime.of(2023, 3, 28, 12, 30),
                orderBook1.getCompanyName(), orderBook1.getOrderBookId().getExchangeId(),
                orderBook1, 10, 0,
                15.00, Order.OrderType.BUY, true, new ArrayList<Trade>());
        buyOrder2 = new Order(2, 1,
                LocalDateTime.of(2023, 3, 28, 12, 35),
                orderBook1.getCompanyName(), orderBook1.getOrderBookId().getExchangeId(),
                orderBook1, 5, 0,
                16.00, Order.OrderType.BUY, true, new ArrayList<Trade>());
        sellOrder1 = new Order(3, 2,
                LocalDateTime.of(2023, 3, 28, 12, 40),
                orderBook1.getCompanyName(), orderBook1.getOrderBookId().getExchangeId(),
                orderBook1, 10, 0,
                14.00, Order.OrderType.SELL, true, new ArrayList<Trade>());
        sellOrder2 = new Order(4, 2,
                LocalDateTime.of(2023, 3, 28, 12, 45),
                orderBook1.getCompanyName(), orderBook1.getOrderBookId().getExchangeId(),
                orderBook1, 15, 0,
                17.00, Order.OrderType.SELL, true, new ArrayList<Trade>());
        sellOrder3 = new Order(5, 2,
                LocalDateTime.of(2023, 3, 28, 12, 45),
                orderBook1.getCompanyName(), orderBook1.getOrderBookId().getExchangeId(),
                orderBook1, 12, 0,
                11.00, Order.OrderType.SELL, true, new ArrayList<Trade>());

        // buyOrder1: qty 10, price 15
        // buyOrder2: qty 5, price 16
        // sellOrder1: qty 10, price 14
        // sellOrder2: qty 15, price 17
        // sellOrder3: qty 12, price 11

    }

    @Test
    public void addOrderWithNoOtherOrders() {
        orderBook1.setOrders(Arrays.asList(buyOrder1));
        orderBook1.matchOrder(buyOrder1);
        Assert.assertEquals(0, exchange1.getTotalTradedValueByDate(
                LocalDate.now()), .01);
    }

    @Test
    public void addOrderWithNonMatchingOrder() {
        orderBook1.setOrders(Arrays.asList(buyOrder1, buyOrder2));
        orderBook1.matchOrder(buyOrder2);
        Assert.assertEquals(0, exchange1.getTotalTradedValueByDate(
                LocalDate.now()), .01);
    }

    @Test
    public void addSellOrderWithOneBuyOrder() {
        orderBook1.setOrders(Arrays.asList(buyOrder1, sellOrder1));
        orderBook1.matchOrder(sellOrder1);
        Assert.assertEquals(150, exchange1.getTotalTradedValueByDate(
                LocalDate.now()), .01);
        Assert.assertEquals(1.0, buyOrder1.getTrades().get(0).getProfitPerShare().doubleValue(), .01);
    }

    @Test
    public void addSellOrderWithWorsePriceThanBuyOrders() {
        orderBook1.setOrders(Arrays.asList(buyOrder1, buyOrder2, sellOrder2));
        orderBook1.matchOrder(sellOrder2);
        Assert.assertEquals(0, exchange1.getTotalTradedValueByDate(
                LocalDate.now()), .01);
    }

    @Test
    public void addSellOrderThatSplitsBuyOrders() {
        orderBook1.setOrders(Arrays.asList(buyOrder1, buyOrder2, sellOrder3));
        orderBook1.matchOrder(sellOrder3);
        Assert.assertEquals(185, exchange1.getTotalTradedValueByDate(
                LocalDate.now()), .01);
        Assert.assertEquals(5.0, sellOrder3.getTrades().get(0).getProfitPerShare().doubleValue(), .01);
        Assert.assertEquals(4.0, sellOrder3.getTrades().get(1).getProfitPerShare().doubleValue(), .01);
    }

    @Test
    public void twoIdenticalBuyOrdersExceptTimestamp() {
        Order buyOrderClone = new Order(6, 1,
                LocalDateTime.of(2023, 3, 28, 12, 0),
                orderBook1.getCompanyName(), orderBook1.getOrderBookId().getExchangeId(),
                orderBook1, 10, 0,
                15.00, Order.OrderType.BUY, true, new ArrayList<Trade>());
        orderBook1.setOrders(Arrays.asList(buyOrder1, buyOrderClone, sellOrder3));
        orderBook1.matchOrder(sellOrder3);
        Assert.assertEquals(180, exchange1.getTotalTradedValueByDate(
                LocalDate.now()), .01);
        Assert.assertEquals(4.0, sellOrder3.getTrades().get(0).getProfitPerShare().doubleValue(), .01);
        Assert.assertEquals(4.0, sellOrder3.getTrades().get(1).getProfitPerShare().doubleValue(), .01);
    }

}
