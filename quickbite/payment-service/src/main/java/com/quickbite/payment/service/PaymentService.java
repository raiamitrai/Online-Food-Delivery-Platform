package com.quickbite.payment.service;

import com.quickbite.payment.dto.AddMoneyRequest;
import com.quickbite.payment.dto.PaymentRequest;
import com.quickbite.payment.dto.PaymentResponse;
import com.quickbite.payment.dto.WalletResponse;

public interface PaymentService {
    WalletResponse createWallet(Long customerId);
    WalletResponse getWallet(Long customerId);
    WalletResponse addMoney(Long customerId, AddMoneyRequest request);
    PaymentResponse processPayment(Long customerId, PaymentRequest request);
    PaymentResponse refundPayment(Long orderId);
}
