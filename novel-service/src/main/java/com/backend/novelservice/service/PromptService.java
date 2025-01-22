package com.backend.novelservice.service;

import com.backend.dto.response.PageResponse;
import com.backend.novelservice.dto.request.PromptRequest;
import com.backend.novelservice.dto.response.PromptResponse;
import com.backend.novelservice.mapper.PromptMapper;
import com.backend.novelservice.repository.PromptRepository;
import com.backend.utils.DateTimeFormatterUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromptService {
    PromptRepository promptRepository;
    PromptMapper promptMapper;
    DateTimeFormatterUtils dateTimeFormatter;

    public PromptResponse createPrompt(PromptRequest request){
        var prompt = promptMapper.toPrompt(request);
        prompt.setCreatedAt(Instant.now());
        return promptMapper.toPromptResponse(promptRepository.save(prompt));
    }
    public PromptResponse updatePrompt(String promptId, PromptRequest request){
        var prompt = promptRepository.findById(promptId).orElseThrow(() -> new IllegalArgumentException("Prompt not found"));
        promptMapper.updatePromptFromRequest(prompt, request);

        return promptMapper.toPromptResponse(promptRepository.save(prompt));
    }
    public void deletePrompt(String promptId){
        promptRepository.deleteById(promptId);
    }
    public PromptResponse getPrompt(String promptId){
        return promptMapper.toPromptResponse(promptRepository.findById(promptId).orElseThrow(() -> new IllegalArgumentException("Prompt not found")));
    }
    public PageResponse<PromptResponse> getAllPrompts(int page, int size){
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = promptRepository.findAll(pageable);
        var promptList = pageData.getContent().stream().map(prompt -> {
            var promptResponse = promptMapper.toPromptResponse(prompt);
            promptResponse.setCreated(dateTimeFormatter.format(prompt.getCreatedAt()));
            return promptResponse;
        }).toList();
        return PageResponse.<PromptResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(promptList)
                .build();
    }
}
