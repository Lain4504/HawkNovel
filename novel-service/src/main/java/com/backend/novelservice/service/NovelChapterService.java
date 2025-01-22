package com.backend.novelservice.service;

import com.backend.dto.response.PageResponse;
import com.backend.novelservice.enums.ChapterStatusEnum;
import com.backend.event.NovelDataSenderEvent;
import com.backend.novelservice.dto.request.NovelChapterRequest;
import com.backend.novelservice.dto.response.NovelChapterResponse;
import com.backend.novelservice.dto.response.NovelElasticResponse;
import com.backend.novelservice.entity.Novel;
import com.backend.novelservice.entity.NovelCategory;
import com.backend.novelservice.entity.NovelChapter;
import com.backend.novelservice.entity.NovelVolume;
import com.backend.novelservice.mapper.NovelChapterMapper;
import com.backend.novelservice.repository.NovelChapterRepository;
import com.backend.novelservice.repository.NovelRepository;
import com.backend.novelservice.repository.NovelVolumeRepository;
import com.backend.utils.DateTimeFormatterUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class NovelChapterService {
    NovelChapterRepository novelChapterRepository;
    NovelChapterMapper novelChapterMapper;
    NovelRepository novelRepository;
    DateTimeFormatterUtils dateTimeFormatter;
    NovelVolumeRepository novelVolumeRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    private int sumChapterCountByNovelId(String novelId) {
        List<NovelVolume> volumes = novelVolumeRepository.findChapterCountsByNovelId(novelId);
        return volumes.stream().mapToInt(NovelVolume::getChapterCount).sum();
    }

    public NovelChapterResponse createChapter(String volumeId, NovelChapterRequest request) {
        // Lấy thông tin NovelVolume theo volumeId
        NovelVolume volume = novelVolumeRepository.findById(volumeId)
                .orElseThrow(() -> new RuntimeException("Volume not found"));

        // Tìm chapter có chapterNumber lớn nhất trong volume
        Optional<NovelChapter> lastChapterOpt = novelChapterRepository.findTopByVolumeIdOrderByChapterNumberDesc(volumeId);
        int nextChapterNumber = lastChapterOpt.map(novelChapter -> novelChapter.getChapterNumber() + 1).orElse(1);

        NovelChapter novelChapter = novelChapterMapper.toNovelChapter(request);
        novelChapter.setVolumeId(volumeId);
        novelChapter.setChapterNumber(nextChapterNumber); // Set chapterNumber tự động tăng
        novelChapter.setCreatedDate(Instant.now());
        NovelChapter savedChapter = novelChapterRepository.save(novelChapter);

        List<String> chapterIds = volume.getChapterIds();
        if (chapterIds == null) {
            chapterIds = new ArrayList<>();
        }
        chapterIds.add(savedChapter.getId());
        volume.setChapterIds(chapterIds);
        volume.setChapterCount(chapterIds.size());
        novelVolumeRepository.save(volume);

        Novel novel = novelRepository.findById(volume.getNovelId())
                .orElseThrow(() -> new RuntimeException("Novel not found"));
        novel.setLatestChapterId(savedChapter.getId());
        novel.setLatestChapterTitle(savedChapter.getChapterTitle());
        novel.setLatestChapterTime(Instant.now());
        novel.setChapterCount(sumChapterCountByNovelId(novel.getId())); // Cập nhật chapterCount
        novel.setWordCount(novel.getWordCount() + savedChapter.getWordCount());
        novelRepository.save(novel);
        sendNovelDataToKafka(novel, "update");
        return novelChapterMapper.toNovelChapterResponse(savedChapter);
    }

    public NovelChapterResponse updateChapter(String chapterId, NovelChapterRequest request) {
        var chapter = novelChapterRepository.findById(chapterId).orElseThrow(() -> new RuntimeException("Chapter not found"));
        long oldWordCount = chapter.getWordCount();
        novelChapterMapper.updateNovelChapter(chapter, request);
        novelChapterRepository.save(chapter);
        long newWordCount = chapter.getWordCount();
        long wordCountDiff = newWordCount - oldWordCount;
        NovelVolume volume = novelVolumeRepository.findById(chapter.getVolumeId()).orElseThrow(() -> new RuntimeException("Volume not found"));
        Novel novel = novelRepository.findById(volume.getNovelId()).orElseThrow(() -> new RuntimeException("Novel not found"));
        novel.setWordCount(novel.getWordCount() + wordCountDiff);
        novel.setLatestChapterId(chapterId);
        novel.setLatestChapterTitle(chapter.getChapterTitle());
        novelRepository.save(novel);
        sendNovelDataToKafka(novel, "update");
        return novelChapterMapper.toNovelChapterResponse(chapter);
    }

    public void deleteChapter(String chapterId) {
        var chapter = novelChapterRepository.findById(chapterId).orElseThrow(() -> new RuntimeException("Chapter not found"));
        var volume = novelVolumeRepository.findById(chapter.getVolumeId()).orElseThrow(() -> new RuntimeException("Volume not found"));
        // Xóa chapter khỏi danh sách chapterIds của volume
        List<String> chapterIds = volume.getChapterIds();
        chapterIds.remove(chapterId);
        volume.setChapterIds(chapterIds);
        volume.setChapterCount(chapterIds.size());
        novelVolumeRepository.save(volume);
        // Cập nhật chapterCount trong Novel
        var novel = novelRepository.findById(volume.getNovelId()).orElseThrow(() -> new RuntimeException("Novel not found"));
        novel.setChapterCount(sumChapterCountByNovelId(novel.getId()));
        novel.setWordCount(novel.getWordCount() - chapter.getWordCount());
        // Check if the chapter is the largest chapter of the largest volume
        boolean isLargestChapter = novelChapterRepository.findTopByVolumeIdOrderByChapterNumberDesc(volume.getId())
                .map(NovelChapter::getId).orElse("").equals(chapterId);
        boolean isLargestVolume = novelVolumeRepository.findTopByNovelIdOrderByVolumeNumberDesc(volume.getNovelId())
                .map(NovelVolume::getId).orElse("").equals(volume.getId());
        // If the chapter is the largest chapter of the largest volume, update the latest chapter title
        if (isLargestChapter && isLargestVolume) {
            Optional<NovelChapter> previousChapterOpt = novelChapterRepository.findTopByVolumeIdAndChapterNumberLessThanOrderByChapterNumberDesc(volume.getId(), chapter.getChapterNumber());
            if (previousChapterOpt.isPresent()) {
                NovelChapter previousChapter = previousChapterOpt.get();
                novel.setLatestChapterId(previousChapter.getId());
                novel.setLatestChapterTitle(previousChapter.getChapterTitle());
            } else {
                novel.setLatestChapterId(null);
                novel.setLatestChapterTitle(null);
            }
        }
        novelRepository.save(novel);
        sendNovelDataToKafka(novel, "update");
        novelChapterRepository.delete(chapter);
    }

    public NovelChapterResponse getChapter(String chapterId) {
        var chapter = novelChapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        var chapterResponse = novelChapterMapper.toNovelChapterResponse(chapter);
        chapterResponse.setCreated(dateTimeFormatter.format(chapter.getCreatedDate()));
        var volume = novelVolumeRepository.findById(chapter.getVolumeId())
                .orElseThrow(() -> new RuntimeException("Volume not found"));
        var novel = novelRepository.findById(volume.getNovelId())
                .orElseThrow(() -> new RuntimeException("Novel not found"));
        chapterResponse.setAuthorId(novel.getAuthorId());
        return chapterResponse;
    }

    public PageResponse<NovelChapterResponse> getChapters(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = novelChapterRepository.findAll(pageable);
        var chapterList = pageData.getContent().stream().map(novelChapter -> {
            var chapterResponse = novelChapterMapper.toNovelChapterResponse(novelChapter);
            chapterResponse.setCreated(dateTimeFormatter.format(Instant.from(novelChapter.getCreatedDate())));
            return chapterResponse;
        }).toList();
        return PageResponse.<NovelChapterResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(chapterList)
                .build();
    }

    public List<NovelChapterResponse> getChaptersByVolumeId(String volumeId, String status) {
        List<NovelChapter> chapters;
        if (status != null && !status.isEmpty()) {
            chapters = novelChapterRepository.findChaptersByVolumeIdWithoutContent(volumeId, status);
            chapters = chapters.stream()
                    .filter(chapter -> chapter.getStatus().toString().equals(status))
                    .sorted(Comparator.comparing(NovelChapter::getChapterNumber))
                    .toList();
        } else {
            chapters = novelChapterRepository.findAllChaptersByVolumeIdWithoutContent(volumeId);
            chapters = chapters.stream()
                    .sorted(Comparator.comparing(NovelChapter::getChapterNumber))
                    .toList();
        }
        return chapters.stream()
                .map(novelChapterMapper::toNovelChapterResponse)
                .toList();
    }

    public void incrementVisitCount(String chapterId) {
        NovelChapter chapter = novelChapterRepository.findById(chapterId).orElseThrow(() -> new RuntimeException("Chapter not found"));
        NovelVolume volume = novelVolumeRepository.findById(chapter.getVolumeId()).orElseThrow(() -> new RuntimeException("Volume not found"));
        Novel novel = novelRepository.findById(volume.getNovelId()).orElseThrow(() -> new RuntimeException("Novel not found"));
        novel.setVisitCount(novel.getVisitCount() + 1);
        novelRepository.save(novel);
    }

    // Lấy chapter theo số thứ tự( hiện tại đang dùng getChapter)
    public Optional<NovelChapter> getChapterByNumber(String volumeId, Integer chapterNumber) {
        return novelChapterRepository.findByVolumeIdAndChapterNumber(volumeId, chapterNumber);
    }

    public NovelChapterResponse getPreviousChapter(String volumeId, Integer currentChapterNumber) {
        Optional<NovelChapter> previousChapterOpt = novelChapterRepository.findTopByVolumeIdAndChapterNumberLessThanOrderByChapterNumberDesc(volumeId, currentChapterNumber);
        if (previousChapterOpt.isPresent() && previousChapterOpt.get().getStatus() != ChapterStatusEnum.DRAFT) {
            return novelChapterMapper.toNovelChapterResponse(previousChapterOpt.get());
        }
        // Nếu không có chapter trước đó trong volume hiện tại, tìm volume trước đó
        Optional<NovelVolume> currentVolumeOpt = novelVolumeRepository.findById(volumeId);
        if (currentVolumeOpt.isEmpty()) {
            return null;
        }
        // Lấy volume trước đó
        NovelVolume currentVolume = currentVolumeOpt.get();
        Optional<NovelVolume> previousVolumeOpt = novelVolumeRepository.findPreviousVolume(currentVolume.getNovelId(), currentVolume.getVolumeNumber());
        if (previousVolumeOpt.isEmpty()) {
            return null;
        }
        NovelVolume previousVolume = previousVolumeOpt.get();
        // Lấy chapter cuối cùng của volume trước đó
        Optional<NovelChapter> lastChapterOpt = novelChapterRepository.findTopByVolumeIdOrderByChapterNumberDesc(previousVolume.getId());
        if (lastChapterOpt.isPresent() && lastChapterOpt.get().getStatus() != ChapterStatusEnum.DRAFT) {
            return novelChapterMapper.toNovelChapterResponse(lastChapterOpt.get());
        }
        return null;
    }

    public NovelChapterResponse getNextChapter(String volumeId, Integer currentChapterNumber) {
        Optional<NovelChapter> nextChapterOpt = novelChapterRepository.findTopByVolumeIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(volumeId, currentChapterNumber);
        if (nextChapterOpt.isPresent() && nextChapterOpt.get().getStatus() != ChapterStatusEnum.DRAFT) {
            return novelChapterMapper.toNovelChapterResponse(nextChapterOpt.get());
        }
        // Nếu không có chapter tiếp theo trong volume hiện tại, tìm volume tiếp theo
        Optional<NovelVolume> currentVolumeOpt = novelVolumeRepository.findById(volumeId);
        if (currentVolumeOpt.isEmpty()) {
            return null;
        }
        // Lấy volume tiếp theo
        NovelVolume currentVolume = currentVolumeOpt.get();
        Optional<NovelVolume> nextVolumeOpt = novelVolumeRepository.findNextVolume(currentVolume.getNovelId(), currentVolume.getVolumeNumber());
        if (nextVolumeOpt.isEmpty()) {
            return null;
        }
        NovelVolume nextVolume = nextVolumeOpt.get();
        // Lấy chapter đầu tiên của volume tiếp theo
        Optional<NovelChapter> firstChapterOpt = novelChapterRepository.findTopByVolumeIdOrderByChapterNumberAsc(nextVolume.getId());
        if (firstChapterOpt.isPresent() && firstChapterOpt.get().getStatus() != ChapterStatusEnum.DRAFT) {
            return novelChapterMapper.toNovelChapterResponse(firstChapterOpt.get());
        }
        return null;
    }

public void reorderChapters(String volumeId, List<String> chapterIds) {
    // Lấy danh sách các chapter theo volumeId
    List<NovelChapter> chapters = novelChapterRepository.findAllByVolumeId(volumeId);

    // Tạo một map để dễ dàng truy cập các chapter theo id
    Map<String, NovelChapter> chapterMap = chapters.stream()
            .collect(Collectors.toMap(NovelChapter::getId, chapter -> chapter));

    // Cập nhật lại chapterNumber theo thứ tự mới
    List<NovelChapter> updatedChapters = new ArrayList<>();
    for (int i = 0; i < chapterIds.size(); i++) {
        String chapterId = chapterIds.get(i);
        NovelChapter chapter = chapterMap.get(chapterId);
        if (chapter != null) {
            chapter.setChapterNumber(i + 1);
            updatedChapters.add(chapter);
        }
    }

    // Lưu lại các chapter với thứ tự mới
    novelChapterRepository.saveAll(updatedChapters);

    // Kiểm tra xem volume này có phải là volume lớn nhất hay không
    NovelVolume volume = novelVolumeRepository.findById(volumeId).orElseThrow(() -> new RuntimeException("Volume not found"));
    Novel novel = novelRepository.findById(volume.getNovelId()).orElseThrow(() -> new RuntimeException("Novel not found"));
    boolean isLargestVolume = novelVolumeRepository.findTopByNovelIdOrderByVolumeNumberDesc(volume.getNovelId())
            .map(NovelVolume::getId).orElse("").equals(volumeId);

    // Nếu volume này là lớn nhất, cập nhật latestChapterTitle
    if (isLargestVolume) {
        Optional<NovelChapter> latestChapterOpt = novelChapterRepository.findTopByVolumeIdOrderByChapterNumberDesc(volumeId);
        if (latestChapterOpt.isPresent()) {
            NovelChapter latestChapter = latestChapterOpt.get();
            novel.setLatestChapterId(latestChapter.getId());
            novel.setLatestChapterTitle(latestChapter.getChapterTitle());
            novelRepository.save(novel);
        }
    }
    sendNovelDataToKafka(novel, "update");
}
    private void sendNovelDataToKafka(Novel novel, String action) {
        NovelElasticResponse elasticResponse = NovelElasticResponse.builder()
                .id(novel.getId())
                .categoryId(novel.getCategories().stream().map(NovelCategory::getId).collect(Collectors.toSet()))
                .categoryName(novel.getCategories().stream().map(NovelCategory::getName).collect(Collectors.toSet()))
                .bookName(novel.getTitle())
                .authorId(novel.getAuthorId())
                .authorName(novel.getAuthorName())
                .score(novel.getScore())
                .status(novel.getStatus().name())
                .visitCount(novel.getVisitCount())
                .wordCount(novel.getWordCount())
                .lastChapterId(novel.getLatestChapterId())
                .image(novel.getImage() != null ? novel.getImage().getPath() : null)
                .lastChapterName(novel.getLatestChapterTitle())
                .lastChapterId(novel.getLatestChapterId())
                .lastChapterUpdateTime(novel.getLatestChapterTime() != null ? novel.getLatestChapterTime().toEpochMilli() : null)
                .build();
        NovelDataSenderEvent event = NovelDataSenderEvent
                .builder()
                .channel("NOVEL")
                .param(Map.of("data", elasticResponse))
                .build();
        kafkaTemplate.send("novel-" + action, event);
    }
}