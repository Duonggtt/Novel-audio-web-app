package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.CoinWallet;
import com.spring3.oauth.jwt.services.CoinWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CoinWalletServiceImpl implements CoinWalletService {
    @Override
    public CoinWallet createNewCoinWallet(CoinWallet request) {
        return null;
    }
}
