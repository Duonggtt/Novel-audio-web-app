package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Tier;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.repositories.TierRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import com.spring3.oauth.jwt.services.TierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TierServiceImpl implements TierService {

    private final TierRepository tierRepository;

    @Override
    public Tier getTierById(long id) {
        return tierRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Tier not found"));
    }

    @Override
    public Tier saveTier(Tier tier) {
        Tier newTier = new Tier();
        newTier.setName(tier.getName());
        newTier.setIconPath(tier.getIconPath());
        newTier.setDescription(tier.getDescription());
        newTier.setReadCountRequired(tier.getReadCountRequired());
        return tierRepository.save(newTier);
    }

    @Override
    public Tier updateTier(Tier tier, long id) {
        Tier existingTier = tierRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Tier not found"));
        existingTier.setName(tier.getName());
        existingTier.setIconPath(tier.getIconPath());
        existingTier.setDescription(tier.getDescription());
        existingTier.setReadCountRequired(tier.getReadCountRequired());
        return tierRepository.save(existingTier);
    }
}
