package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Tier;

public interface TierService {
    Tier getTierById(long id);
    Tier saveTier(Tier tier);
    Tier updateTier(Tier tier, long id);
}
