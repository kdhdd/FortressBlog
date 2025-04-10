package com.example.blog.dto;

import com.example.blog.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ArticleResponse {

    private final String title;
    private final String content;
    private final String imageUrls;

    public ArticleResponse(Article article) {
        this.title = article.getTitle();
        this.content = article.getContent();
        this.imageUrls = article.getImageUrl();
    }
}
