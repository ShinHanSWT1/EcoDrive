package com.gorani.market.service;

public record GoraniAccountResponse(
        Long id,
        Long payUserId,
        String accountNumber,
        String bankCode,
        String ownerName,
        Integer balance,
        String status
) {
}

