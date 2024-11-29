package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.CoinWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinWalletRepository extends JpaRepository<CoinWallet, String>{
}
