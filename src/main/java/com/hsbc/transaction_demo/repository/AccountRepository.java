package com.hsbc.transaction_demo.repository;

import com.hsbc.transaction_demo.model.Account;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class AccountRepository {
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    public Account save(Account account) {
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    public List<Account> findAll() {
        return accounts.values().stream().collect(Collectors.toList());
    }

    public void delete(Account account) {
        accounts.remove(account.getAccountNumber());
    }

    public boolean existsByAccountNumber(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }
} 