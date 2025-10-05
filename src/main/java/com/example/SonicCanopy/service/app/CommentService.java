package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.domain.dto.comment.CommentDto;
import com.example.SonicCanopy.domain.dto.comment.CommentUserInfo;
import com.example.SonicCanopy.domain.dto.comment.CreateCommentRequest;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.*;
import com.example.SonicCanopy.domain.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.domain.exception.comment.CommentLikeException;
import com.example.SonicCanopy.domain.exception.comment.CommentNotFoundException;
import com.example.SonicCanopy.domain.exception.event.EventNotFoundException;
import com.example.SonicCanopy.domain.mapper.CommentMapper;
import com.example.SonicCanopy.repository.ClubRepository;
import com.example.SonicCanopy.repository.CommentLikeRepository;
import com.example.SonicCanopy.repository.CommentRepository;
import com.example.SonicCanopy.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;
    private final ClubAuthorizationService clubAuthorizationService;

    private final CommentMapper commentMapper;

    private final Clock clock;

    public CommentService(CommentRepository commentRepository, CommentLikeRepository commentLikeRepository,
                          EventRepository eventRepository,
                          Clock clock,
                          CommentMapper commentMapper,
                          ClubAuthorizationService clubAuthorizationService,
                          ClubRepository clubRepository
    ) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.eventRepository = eventRepository;
        this.clock = clock;
        this.commentMapper = commentMapper;
        this.clubAuthorizationService = clubAuthorizationService;
        this.clubRepository = clubRepository;
    }

    @Transactional
    public CommentDto createComment(CreateCommentRequest request, User requester, Long clubId, Long eventId){
         if(clubRepository.isPrivate(clubId) && !clubAuthorizationService.isMember(requester.getId(),  clubId)){
             throw new UnauthorizedActionException("You are not a member of this private club.");
         }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new EventNotFoundException("Cannot find the event")
        );

        Comment parent = request.parentId() == null ? null : commentRepository.findById(request.parentId()).orElseThrow(
                () -> new CommentNotFoundException("Cannot find the comment you want to reply")
        );

        UUID uuid = UUID.randomUUID();

        Comment comment = Comment.builder()
                .uuid(uuid)
                .content(request.content())
                .event(event)
                .createdBy(requester)
                .createdAt(LocalDateTime.now(clock))
                .parent(parent)
                .build();

        String path = parent == null ?
                "/" + uuid:
                parent.getPath() + "/" + uuid;

        comment.setPath(path);

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment, getUserInfo(savedComment.getCreatedBy()), eventId);
    }

    @Transactional
    public void softDeleteComment(UUID uuid, Long clubId, User requester){
        clubAuthorizationService.isMemberOrThrow(requester.getId(), clubId, "You are not allowed to delete this comment.");

        Comment comment = commentRepository.findByUuid(uuid).orElseThrow(
                () -> new CommentNotFoundException("Cannot find the comment with uuid: " + uuid)
        );

        if(!requester.getId().equals(comment.getCreatedBy().getId())) {
            clubAuthorizationService.authorize(clubId, requester.getId(), Privilege.DELETE_COMMENTS);
        }

        comment.setDeleted(true);
    }

    @Transactional
    public void hardDeleteComment(UUID uuid, Long clubId, User requester){
        clubAuthorizationService.isMemberOrThrow(requester.getId(), clubId, "You are not authorized to delete this comment.");

        Comment comment = commentRepository.findByUuid(uuid).orElseThrow(
                () -> new CommentNotFoundException("Cannot find the comment with uuid: " + uuid)
        );

        clubAuthorizationService.authorize(clubId, requester.getId(), Privilege.DELETE_COMMENTS);

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentDto> getRootComments(User requester, Long clubId, Long eventId, int page, int size, HttpServletRequest request) {
        if(clubRepository.isPrivate(clubId) && !clubAuthorizationService.isMember(requester.getId(),  clubId)){
            throw new UnauthorizedActionException("You are not a member of this private club.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("numberOfLikes"),
                Sort.Order.asc("createdAt")
        ));

        Page<Comment> commentPage = commentRepository.findByEventIdAndParentIsNull(eventId, pageable);

        List<CommentDto> commentDtoList = mapCommentsInPageToDtoList(commentPage, eventId);

        return PagedResponse.of(
                commentDtoList,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                request
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentDto> getRepliesFlattened(User requester, UUID rootUuid, Long clubId, Long eventId, int page, int size, HttpServletRequest request) {
        if(clubRepository.isPrivate(clubId) && !clubAuthorizationService.isMember(requester.getId(),  clubId)){
            throw new UnauthorizedActionException("You are not a member of this private club.");
        }

        Comment rootComment = commentRepository.findByUuid(rootUuid)
                .orElseThrow(() -> new CommentNotFoundException("Root comment not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.asc("createdAt")
        ));

        Page<Comment> commentPage = commentRepository.findRepliesFlattened(rootComment.getPath(), rootComment.getId(), pageable);

        List<CommentDto> commentDtoList = mapCommentsInPageToDtoList(commentPage, eventId);

        return PagedResponse.of(
                commentDtoList,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                request
        );
    }

    @Transactional
    public void likeComment(User requester, UUID commentUuid) {
        Comment comment = commentRepository.findByUuid(commentUuid).orElseThrow(
                () -> new CommentNotFoundException("Comment does not exit")
        );

        if(commentLikeRepository.existsByCommentAndUser(comment, requester)){
            throw new CommentLikeException("You liked the message already");
        }

        commentLikeRepository.save(new CommentLike(null,comment, requester, LocalDateTime.now(clock)));
        comment.setNumberOfLikes(comment.getNumberOfLikes() + 1);
    }

    @Transactional
    public void unlikeComment(User requester, UUID commentUuid) {
        Comment comment = commentRepository.findByUuid(commentUuid).orElseThrow(
                () -> new CommentNotFoundException("Comment does not exit")
        );

        if(!commentLikeRepository.existsByCommentAndUser(comment, requester)){
            throw new CommentLikeException("You have not liked the message before");
        }

        commentLikeRepository.deleteByCommentAndUser(comment, requester);
    }

    private List<CommentDto> mapCommentsInPageToDtoList(Page<Comment> commentPage, Long eventId) {
        return commentPage.getContent().stream()
                .map(comment -> {
                    CommentUserInfo userInfo = getUserInfo(comment.getCreatedBy());
                    return commentMapper.toDto(comment, userInfo, eventId);
                })
                .toList();
    }

    private CommentUserInfo getUserInfo(User commenter){
        String href = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/users/{username}")
                .buildAndExpand(commenter.getUsername())
                .toUriString();

        return CommentUserInfo.builder()
                .username(commenter.getUsername())
                //TODO: .profileImageUrl(commentor.imageUrl())
                .userHref(href)
                .build();
    }
}
