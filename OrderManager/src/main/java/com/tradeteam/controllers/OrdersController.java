package com.tradeteam.controllers;

import com.tradeteam.dtos.OrderBookId;
import com.tradeteam.entities.Order;
import com.tradeteam.entities.OrderBook;
import com.tradeteam.dtos.TradeDTO;
import com.tradeteam.entities.Trade;
import com.tradeteam.entities.WalletItem;
import com.tradeteam.security.OrderManagerUserDetails;
import com.tradeteam.services.ExchangeOrderBookService;
import com.tradeteam.services.OrderService;
import com.tradeteam.services.TradingEngineTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrdersController {
    @Autowired
    OrderService orderService;

    @Autowired
    ExchangeOrderBookService exchangeOrderBookService;

    @Autowired
    TradingEngineTradeService tradingEngineTradeService;

    @GetMapping("/orders")
    public String findByUserId(@AuthenticationPrincipal OrderManagerUserDetails userDetails,
                               Model model) {
        List<Order> orders = orderService.findByUserId(userDetails.getUserId());
        model.addAttribute("orders", orders);
        return "list_orders";
    }

    @GetMapping("/order/create")
    public String addNewOrder(Model model) {
        HashMap<String, List<String>> exchangeCompanyAbbrevs = tradingEngineTradeService.getExchangeIdsAndCompanyAbbrevs();
        Map<String, Map<String, Double[]>> bestBuyAndSellPrices =
                exchangeOrderBookService.getBestBuyAndSellPrices();
        model.addAttribute("exchangeCompanyAbbrevs", exchangeCompanyAbbrevs);
        model.addAttribute("order", new Order());
        model.addAttribute("bestBuyAndSellPrices", bestBuyAndSellPrices);
        return "add_order";
    }

    @PostMapping("/order/create")
    public String createOrder(@AuthenticationPrincipal OrderManagerUserDetails userDetails,
                              @RequestParam("numberOrdered") int numberOrdered,
                              @RequestParam("price") double price,
                              @RequestParam("OrderType") String orderType,
                              @RequestParam("companyAbbrev") String companyAbbrev,
                              @RequestParam("exchangeId") String exchangeId) {
        Order newOrder = new Order(numberOrdered, price, orderType, userDetails.getUserId(), companyAbbrev, exchangeId);
        orderService.createOrder(newOrder);
        return "redirect:/orders";
    }

    @PostMapping("/order/cancel")
    public String cancelOrder(
            @AuthenticationPrincipal OrderManagerUserDetails userDetails,
            @RequestParam("orderId") int orderId) {
        int currentUserId = userDetails.getUserId();
        orderService.cancelOrder(orderId, currentUserId);
        return "redirect:/orders";
    }

    @GetMapping("/order/edit/{orderId}")
    public String editOrder(@PathVariable("orderId") int orderId,
                            Model model) {
        Order order = orderService.findById(orderId);
        HashMap<String, List<String>> exchangeCompanyAbbrevs = tradingEngineTradeService
                .getExchangeIdsAndCompanyAbbrevs();
        Map<String, Map<String, Double[]>> bestBuyAndSellPrices =
                exchangeOrderBookService.getBestBuyAndSellPrices();
        model.addAttribute("exchangeCompanyAbbrevs", exchangeCompanyAbbrevs);
        model.addAttribute("order", order);
        model.addAttribute("bestBuyAndSellPrices", bestBuyAndSellPrices);
        model.addAttribute("orderType", order.getOrderType().toString());
        return "edit_order";
    }

    @PostMapping("/order/update/{orderId}")
    public String updateOrder(@PathVariable("orderId") int orderId,
                              @RequestParam("numberOrdered") int numberOrdered,
                              @RequestParam("price") double price,
                              @RequestParam("OrderType") String orderType,
                              @RequestParam("exchangeId") String exchangeId,
                              @RequestParam("companyAbbrev") String companyAbbrev) {
        orderService.updateOrder(orderId, numberOrdered, price, orderType, exchangeId, companyAbbrev);
        return "redirect:/orders";
    }

    @GetMapping("/order/{orderId}")
    public String getOrderDetails(@PathVariable("orderId") int orderId, Model model) {
        Order order = orderService.findById(orderId);
        model.addAttribute("order", order);
        return "view_order";
    }

    @GetMapping("/order/tradeHistory")
    public String getTradeHistory(@AuthenticationPrincipal OrderManagerUserDetails userDetails,
                                  Model model) {
        int currentUserId = userDetails.getUserId();
        List<Trade> trades = tradingEngineTradeService.getTrades(currentUserId);
        model.addAttribute("trades", trades);
        return "list_trade_history";
    }

    @GetMapping("/exchanges")
    public String getExchangeIds(Model model) {
        List<String> exchangeIds = exchangeOrderBookService.getAllExchangeIds();
        model.addAttribute("exchangeIds", exchangeIds);
        return "exchanges";
    }

    @GetMapping("/orderBooks/{exchangeId}")
    public String getSymbols(@PathVariable String exchangeId, Model model) {
        List<String> symbols = exchangeOrderBookService
                .getCompanyAbbrevsByExchangeId(exchangeId);
        model.addAttribute("symbols", symbols);
        model.addAttribute("exchangeId", exchangeId);
        return "list_order_books";
    }

    @GetMapping("/orderBook/{exchangeId}/{companyAbbrev}")
    public String getOrderBook(@PathVariable String exchangeId,
                               @PathVariable String companyAbbrev, Model model) {
        OrderBook orderBook = exchangeOrderBookService
                .getOrderBookByExchangeIdCompanyAbbrev(exchangeId, companyAbbrev);
        model.addAttribute("orderBook", orderBook);
        return "order_book"; // This view hasn't been made yet
    }

    @GetMapping("/orders/wallet")
    public String getUserWallet(@AuthenticationPrincipal OrderManagerUserDetails userDetails,
                  Model model) {
        List<WalletItem> wallet = tradingEngineTradeService
                .getWalletByUserId(userDetails.getUserId());
        model.addAttribute("wallet", wallet);
        return "wallet";
    }

}
