package com.fastcampus.fcboard.controller

import com.fastcampus.fcboard.controller.dto.CommentCreateRequest
import com.fastcampus.fcboard.controller.dto.CommentUpdateRequest
import com.fastcampus.fcboard.controller.dto.toDto
import com.fastcampus.fcboard.service.CommentService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController(
    private val commentService: CommentService,
) {
    @PostMapping("/posts/{postId}/comments")
    fun createComment(
        @PathVariable postId: Long,
        @RequestBody commentCreateRequest: CommentCreateRequest,
    ): Long {
        println(commentCreateRequest.content)
        println(commentCreateRequest.createdBy)
        return commentService.createComment(postId, commentCreateRequest.toDto())
    }

    @PutMapping("/comments/{commentId}")
    fun updateComment(
        @PathVariable commentId: Long,
        @RequestBody commentUpdateRequest: CommentUpdateRequest,
    ): Long {
        println(commentUpdateRequest.content)
        println(commentUpdateRequest.updatedBy)
        return commentService.updateComment(commentId, commentUpdateRequest.toDto())
    }

    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        @RequestParam deletedBy: String,
    ): Long {
        println(deletedBy)
        return commentService.deleteComment(commentId, deletedBy)
    }
}
