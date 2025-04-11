package com.example.blog.dto;

import com.example.blog.domain.Article;
import lombok.Getter;

import java.util.List;

@Getter
public class ArticleListViewResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final List<String> imageUrls;

    public ArticleListViewResponse(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.imageUrls = article.getImageUrls();
    }
}
