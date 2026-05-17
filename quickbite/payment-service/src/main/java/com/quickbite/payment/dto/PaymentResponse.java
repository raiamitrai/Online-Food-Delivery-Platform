package com.quickbite.payment.dto;

import com.quickbite.payment.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long transactionId;
    private Long orderId;
    private Double amount;
    private TransactionStatus status;
    private String message;
}
