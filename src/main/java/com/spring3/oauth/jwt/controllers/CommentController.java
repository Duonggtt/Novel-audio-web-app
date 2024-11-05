package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.request.CreateCommentRequest;
import com.spring3.oauth.jwt.services.impl.CommentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentServiceImpl commentService;

    @GetMapping("/{slug}")
    public ResponseEntity<?> getAllCommentsInNovel(@PathVariable String slug) {
        return ResponseEntity.ok(commentService.getAllCommentsInNovel(slug));
    }

    @PostMapping("/post-comment")
    public ResponseEntity<?> saveComment(@RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(commentService.saveComment(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    // API mới: Trả lời bình luận
    @PostMapping("/reply")
    public ResponseEntity<?> saveReply(@RequestParam Integer parentCommentId, @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(commentService.saveReply(parentCommentId, request));
    }
}
