package com.kmgServer.project.service;

import com.kmgServer.project.dto.RefreshDTO;
import com.kmgServer.project.repository.RefreshRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {
    private final RefreshRepository refreshRepository;

    public void CreateRefresh(RefreshDTO refreshDTO){
        refreshRepository.CreateRefresh(refreshDTO);
    }

    public boolean existsByRefresh(String refresh){
        return refreshRepository.existsByRefresh(refresh);
    }

    public void deleteRefresh(String refresh){
        refreshRepository.deleteRefresh(refresh);
    }
}
