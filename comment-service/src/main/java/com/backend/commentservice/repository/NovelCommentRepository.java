package com.backend.commentservice.repository;

import com.backend.commentservice.entity.NovelComment;
import com.backend.commentservice.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NovelCommentRepository extends MongoRepository<NovelComment, String> {

    Page<NovelComment> findAllByNovelId(String novelId, Pageable pageable);
}
