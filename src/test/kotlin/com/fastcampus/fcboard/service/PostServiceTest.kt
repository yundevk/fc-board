package com.fastcampus.fcboard.service

import com.fastcampus.fcboard.domain.Comment
import com.fastcampus.fcboard.domain.Post
import com.fastcampus.fcboard.exception.PostNotDeleteException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.exception.PostNotUpdatableException
import com.fastcampus.fcboard.repository.CommentRepository
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.repository.TagRepository
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.service.dto.PostSearchRequestDto
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class PostServiceTest(
    private val postService: PostService,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val tagRepository: TagRepository,
) : BehaviorSpec({
    beforeSpec {
        postRepository.saveAll(
            listOf(
                Post(title = "title1", content = "content1", createdBy = "margo1"),
                Post(title = "title12", content = "content2", createdBy = "margo1"),
                Post(title = "title13", content = "content3", createdBy = "margo1"),
                Post(title = "title14", content = "content4", createdBy = "margo1"),
                Post(title = "title15", content = "content5", createdBy = "margo1"),
                Post(title = "title6", content = "content6", createdBy = "margo2"),
                Post(title = "title7", content = "content7", createdBy = "margo2"),
                Post(title = "title8", content = "content8", createdBy = "margo2"),
                Post(title = "title9", content = "content9", createdBy = "margo2"),
                Post(title = "title10", content = "content10", createdBy = "margo2")
            )
        )
    }
    given("게시글 생성시") {
        When("게시글 생성") {
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "제목",
                    content = "내용",
                    createdBy = "margo"
                )
            )
            then("게시글이 정상적으로 생성됨을 확인한다.") {
                postId shouldBeGreaterThan 0L
                val post = postRepository.findByIdOrNull(postId)
                post shouldNotBe null
                post?.title shouldBe "제목"
                post?.content shouldBe "내용"
                post?.createdBy shouldBe "margo"
            }
        }
        When("태그가 추가되면") {
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "제목",
                    content = "내용",
                    createdBy = "margo",
                    tags = listOf("tag1", "tag2")
                )
            )
            then("태그가 정상적으로 추가됨을 확인한다.") {
                val tags = tagRepository.findByPostId(postId)
                tags.size shouldBe 2
                tags[0].name shouldBe "tag1"
                tags[1].name shouldBe "tag2"
            }
        }
    }
    given("게시글 수정시") {
        val saved = postRepository.save(
            Post(
                title = "제목",
                content = "내용",
                createdBy = "margo",
                tags = listOf("tag1", "tag2")
            )
        )
        When("정상 수정시") {
            val updatedId = postService.updatePost(
                saved.id,
                PostUpdateRequestDto(
                    title = "update title",
                    content = "update content",
                    updatedBy = "margo"
                )
            )
            then("게시글이 정상적으로 수정됨을 확인한다.") {
                saved.id shouldBe updatedId
                val updated = postRepository.findByIdOrNull(updatedId)
                updated shouldNotBe null
                updated?.title shouldBe "update title"
                updated?.content shouldBe "update content"
//                updated?.updatedBy shouldBe "update margo"
            }
        }
        When("게시글이 없을 때") {
            then("게시글을 찾을 수 없다는 예외가 발생한다.") {
                shouldThrow<PostNotFoundException> {
                    postService.updatePost(
                        9999L,
                        PostUpdateRequestDto(
                            title = "update title",
                            content = "update content",
                            updatedBy = "update margo"
                        )
                    )
                }
            }
        }
        When("작성자가 동일하지 않으면") {
            then("수정할 수 없는 게시물 예외가 발생한다.") {
                shouldThrow<PostNotUpdatableException> {
                    postService.updatePost(
                        1L,
                        PostUpdateRequestDto(
                            title = "update title",
                            content = "update content",
                            updatedBy = "update margo"
                        )
                    )
                }
            }
        }
        When("태그가 수정되었을 때") {
            val updatedId = postService.updatePost(
                saved.id,
                PostUpdateRequestDto(
                    title = "update title",
                    content = "update content",
                    updatedBy = "margo",
                    tags = listOf("tag1", "tag2", "tag3")
                )
            )
            then("정상적으로 수정됨을 확인한다.") {
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tag3"
            }
            then("태그 순서가 변경되었을 때 정상적으로 변경됨을 확인한다.") {
                postService.updatePost(
                    saved.id,
                    PostUpdateRequestDto(
                        title = "update title",
                        content = "update content",
                        updatedBy = "margo",
                        tags = listOf("tag3", "tag2", "tag1")
                    )
                )
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tag1"
            }
        }
    }
    given("게시글 삭제시") {
        val saved = postRepository.save(
            Post(
                title = "제목",
                content = "내용",
                createdBy = "margo"
            )
        )
        When("정상 삭제시") {
            val postId = postService.deletePost(saved.id, "margo")
            then("게시글이 정상적으로 삭제됨을 확인한다.") {
                postId shouldBe saved.id
                postRepository.findByIdOrNull(postId) shouldBe null
            }
        }
        When("작성자가 동일하지 않으면") {
            val saved = postRepository.save(
                Post(
                    title = "제목",
                    content = "내용",
                    createdBy = "margo"
                )
            )
            then("수정할 수 없는 게시물 예외가 발생한다.") {
                shouldThrow<PostNotDeleteException> { postService.deletePost(saved.id, "margo22") }
            }
        }
    }
    given("게시글 상세 조회시") {
        val saved = postRepository.save(
            Post(
                title = "title",
                content = "content",
                createdBy = "margo"
            )
        )
        When("정상 조회시") {
            val post = postService.getPost(saved.id)
            then("게시글 내용이 정상적으로 반환됨을 확인한다.") {
                post.id shouldBe saved.id
                post.title shouldBe "title"
                post.content shouldBe "content"
                post.createdBy shouldBe "margo"
            }
        }
        When("게시글이 없을 때") {
            then("게시글을 찾을 수 없다는 예외가 발생한다.") {
                shouldThrow<PostNotFoundException> {
                    postService.getPost(9999L)
                }
            }
        }
        When("댓글 추가시") {
            commentRepository.save(Comment(content = "댓글 내용1", createdBy = "댓글 작성자", post = saved))
            commentRepository.save(Comment(content = "댓글 내용2", createdBy = "댓글 작성자", post = saved))
            commentRepository.save(Comment(content = "댓글 내용3", createdBy = "댓글 작성자", post = saved))
            val post = postService.getPost(saved.id)
            then("댓글이 함깨 조회됨을 확인한다.") {
                post.comments.size shouldBe 3
                post.comments[0].content shouldBe "댓글 내용1"
                post.comments[1].content shouldBe "댓글 내용2"
                post.comments[2].content shouldBe "댓글 내용3"
                post.comments[0].createdBy shouldBe "댓글 작성자"
                post.comments[1].createdBy shouldBe "댓글 작성자"
                post.comments[2].createdBy shouldBe "댓글 작성자"
            }
        }
    }
    given("게시글 목록 조회시") {
        When("정상 조회시") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto())
            then("게시글 페이지가 반환된다.") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title"
                postPage.content[0].createdBy shouldBe "margo"
            }
        }
        When("타이틀로 검색") {
            then("타이틀에 해당하는 게시글이 반환된다.") {
                val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(title = "title1"))
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title"
                postPage.content[0].createdBy shouldContain "margo"
            }
        }
        When("작성자로 검색") {
            then("작성자에 해당하는 게시글이 반환된다.") {
                val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(createdBy = "margo1"))
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title1"
                postPage.content[0].createdBy shouldBe "margo1"
            }
        }
    }
})
