package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.CoinWallet;

public interface CoinWalletService {
    CoinWallet createNewCoinWallet(CoinWallet coinWallet);
}
