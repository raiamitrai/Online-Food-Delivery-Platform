package com.quickbite.payment.controller;

import com.quickbite.payment.dto.AddMoneyRequest;
import com.quickbite.payment.dto.PaymentRequest;
import com.quickbite.payment.dto.PaymentResponse;
import com.quickbite.payment.dto.WalletResponse;
import com.quickbite.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/wallet/{customerId}")
    public ResponseEntity<WalletResponse> createWallet(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.createWallet(customerId));
    }

    @GetMapping("/wallet/{customerId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getWallet(customerId));
    }

    @PostMapping("/wallet/{customerId}/add")
    public ResponseEntity<WalletResponse> addMoney(@PathVariable Long customerId, @RequestBody AddMoneyRequest request) {
        return ResponseEntity.ok(paymentService.addMoney(customerId, request));
    }

    @PostMapping("/pay/{customerId}")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long customerId, @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(customerId, request));
    }

    @PostMapping("/refund/{orderId}")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.refundPayment(orderId));
    }
}
