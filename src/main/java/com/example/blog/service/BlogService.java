package com.example.blog.service;

import com.example.blog.domain.Article;
import com.example.blog.domain.Comment;
import com.example.blog.domain.User;
import com.example.blog.dto.AddArticleRequest;
import com.example.blog.dto.AddCommentReqeust;
import com.example.blog.dto.UpdateArticleRequest;
import com.example.blog.repository.BlogRepository;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public Article save(AddArticleRequest request, List<MultipartFile> files, String userName) {
        List<String> urls = (files == null || files.isEmpty()) ? List.of() : files.stream()
                .map(multipartFile -> {
                    try {
                        return s3Uploader.upload(multipartFile, "recommend");
                    } catch (IOException e) {
                        throw new RuntimeException("파일 업로드 중 오류 발생: " + e.getMessage(), e);
                    }
                })
                .toList();

        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(userName)
                .imageUrl(String.join(",", urls))
                .build();

        return blogRepository.save(article);
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
    }

    public void delete(Long id) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        blogRepository.delete(article);
    }

    @Transactional
    public Article update(Long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    // 게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!article.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

    /*
     ------------------------------------------------------------------------------------------
                                    댓글 작성 로직
     ------------------------------------------------------------------------------------------
     */

    public Comment addComment(AddCommentReqeust reqeust, String userName) {
        Article article = blogRepository.findById(reqeust.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("not found : " + reqeust.getArticleId()));

        return commentRepository.save(reqeust.toEntity(userName, article));
    }
}
