package com.gorani.market.service;

public record GoraniCreateAccountRequest(
        Long externalUserId,
        String userName,
        String email,
        String ownerName,
        String bankCode,
        String accountNumber
) {
}

