package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.CoinWallet;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.repositories.CoinWalletRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
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
    private final UserRepository userRepository;
    private final CoinWalletRepository coinWalletRepository;

    @Override
    public CoinWallet createNewCoinWallet(CoinWallet request) {
        return null;
    }

    public void addCoinToWallet(int coin, long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        CoinWallet coinWallet = user.getWallet();
        coinWallet.setCoinAmount(coinWallet.getCoinAmount() + coin);
        coinWalletRepository.save(coinWallet);
    }
}
