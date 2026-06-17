package com.xdev.ooms.inventory.articlesec.controller;

import com.xdev.ooms.inventory.Enum.CategorieArticle;
import com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto;
import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.articlesec.service.ArticleSecService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.qr.model.QrResolveResponse;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/articles")
public class ArticleSecController extends BaseControllerImpl<ArticleSec, ArticleSecDto, ArticleSecDto> {

    private final ArticleSecService articleService;

    @Autowired
    public ArticleSecController(ArticleSecService articleService, ModelMapper modelMapper) {
        super(articleService, modelMapper);
        this.articleService = articleService;
    }

    @Transactional(readOnly = true)
    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<?> getArticlesByCategorie(@PathVariable CategorieArticle categorie) {
        try {
            return ResponseEntity.ok(attachPermittedActions(articleService.getArticlesByCategorie(categorie)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getArticlesByCategorie", e);
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/actifs")
    public ResponseEntity<?> getActiveArticles() {
        try {
            return ResponseEntity.ok(attachPermittedActions(articleService.getAllActiveArticles()));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "getActiveArticles", e);
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerArticle(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(articleService.activerArticle(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "activerArticle", e);
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverArticle(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(attachPermittedActions(articleService.desactiverArticle(id)));
        } catch (Exception e) {
            return ExceptionHandler.handleException(this.getClass(), "desactiverArticle", e);
        }
    }

    @GetMapping("/{id}/qr-image")
    public ResponseEntity<byte[]> getQrImage(@PathVariable UUID id) {
        ArticleSec entity = articleService.getArticleEntityById(id);
        byte[] image = articleService.generateQrImage(entity.getQrHex());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    @Override
    protected String getResourceName() {
        return "ARTICLE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        try {
            QrResolveResponse response = getBaseService().resolve(publicCode);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ExceptionHandler.handleException(this.getClass(), "resolve", e);
        }
    }
}
