package com.example.blog.controller;

import com.example.blog.domain.Article;
import com.example.blog.domain.Comment;
import com.example.blog.dto.*;
import com.example.blog.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class BlogApiController {

    private final BlogService blogService;

    @PostMapping(path = "/articles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Article> addArticle(@RequestPart("articleDto") AddArticleRequest request,
                                              @RequestPart(value = "files", required = false)List<MultipartFile> files, Principal principal) {
        Article savedArticle = blogService.save(request, files, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    @GetMapping("/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles() {
        List<ArticleResponse> articles = blogService.findAll()
                .stream()
                .map(ArticleResponse::new)
                .toList();

        return ResponseEntity.ok()
                .body(articles);
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable("id") Long id) {
        Article article = blogService.findById(id);

        return ResponseEntity.ok()
                .body(new ArticleResponse(article));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable("id") Long id) {
        blogService.delete(id);

        return ResponseEntity.ok()
                .build();
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<Article> updateArticle(
            @PathVariable("id") Long id,
            @RequestBody UpdateArticleRequest request) {

        Article updatedArticle = blogService.update(id, request);

        return ResponseEntity.ok()
                .body(updatedArticle);
    }

        /*
     ------------------------------------------------------------------------------------------
                                    댓글 컨트롤러 로직
     ------------------------------------------------------------------------------------------
     */

    @PostMapping("/comments")
    public ResponseEntity<AddCommentResponse> addComment(@RequestBody AddCommentReqeust reqeust, Principal principal) {
        Comment savedComment = blogService.addComment(reqeust, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddCommentResponse(savedComment));
    }
}
