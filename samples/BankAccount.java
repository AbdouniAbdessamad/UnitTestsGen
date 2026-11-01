package com.example.demo;

public class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        if (initialBalance < 0) throw new IllegalArgumentException("Solde initial négatif");
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Montant invalide");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Montant invalide");
        if (amount > balance) throw new IllegalStateException("Solde insuffisant");
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }
}
