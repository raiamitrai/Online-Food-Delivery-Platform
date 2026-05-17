package com.quickbite.payment.service;

import com.quickbite.payment.dto.AddMoneyRequest;
import com.quickbite.payment.dto.PaymentRequest;
import com.quickbite.payment.dto.PaymentResponse;
import com.quickbite.payment.dto.WalletResponse;
import com.quickbite.payment.entity.Transaction;
import com.quickbite.payment.entity.TransactionStatus;
import com.quickbite.payment.entity.TransactionType;
import com.quickbite.payment.entity.Wallet;
import com.quickbite.payment.repository.TransactionRepository;
import com.quickbite.payment.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public PaymentServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public WalletResponse createWallet(Long customerId) {
        if(walletRepository.findByCustomerId(customerId).isPresent()){
            throw new RuntimeException("Wallet already exists");
        }
        Wallet wallet = Wallet.builder().customerId(customerId).balance(0.0).build();
        Wallet savedWallet = walletRepository.save(wallet);
        return mapToWalletResponse(savedWallet);
    }

    @Override
    public WalletResponse getWallet(Long customerId) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return mapToWalletResponse(wallet);
    }

    @Override
    public WalletResponse addMoney(Long customerId, AddMoneyRequest request) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewWallet(customerId));
                
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .walletId(savedWallet.getId())
                .amount(request.getAmount())
                .type(TransactionType.CREDIT)
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return mapToWalletResponse(savedWallet);
    }

    @Override
    public PaymentResponse processPayment(Long customerId, PaymentRequest request) {
        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            return PaymentResponse.builder()
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .status(TransactionStatus.SUCCESS)
                    .message("COD selected. Pay on delivery.")
                    .build();
        }

        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < request.getAmount()) {
            Transaction failedTx = Transaction.builder()
                    .walletId(wallet.getId())
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .type(TransactionType.DEBIT)
                    .status(TransactionStatus.FAILED)
                    .timestamp(LocalDateTime.now())
                    .build();
            transactionRepository.save(failedTx);
            
            throw new RuntimeException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .walletId(wallet.getId())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();
        Transaction savedTx = transactionRepository.save(tx);

        return PaymentResponse.builder()
                .transactionId(savedTx.getId())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(TransactionStatus.SUCCESS)
                .message("Payment successful from Wallet")
                .build();
    }

    @Override
    public PaymentResponse refundPayment(Long orderId) {
        List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
        
        // Find successful debit transaction for this order
        Transaction debitTx = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT && t.getStatus() == TransactionStatus.SUCCESS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No successful payment found for this order"));

        Wallet wallet = walletRepository.findById(debitTx.getWalletId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Refund to wallet
        wallet.setBalance(wallet.getBalance() + debitTx.getAmount());
        walletRepository.save(wallet);

        Transaction refundTx = Transaction.builder()
                .walletId(wallet.getId())
                .orderId(orderId)
                .amount(debitTx.getAmount())
                .type(TransactionType.CREDIT)
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();
        Transaction savedTx = transactionRepository.save(refundTx);

        return PaymentResponse.builder()
                .transactionId(savedTx.getId())
                .orderId(orderId)
                .amount(savedTx.getAmount())
                .status(TransactionStatus.SUCCESS)
                .message("Refund successful")
                .build();
    }

    private Wallet createNewWallet(Long customerId) {
        return walletRepository.save(Wallet.builder().customerId(customerId).balance(0.0).build());
    }

    private WalletResponse mapToWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .customerId(wallet.getCustomerId())
                .balance(wallet.getBalance())
                .build();
    }
}
