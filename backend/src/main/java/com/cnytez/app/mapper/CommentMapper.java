package com.cnytez.app.mapper;

import com.cnytez.app.dto.internal.CommentDto;
import com.cnytez.app.dto.response.VoteResponse;
import com.cnytez.app.model.Comment;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CommentMapper {

    @Mapping(target = "postId", expression = "java(comment.getPost().getId())")
    @Mapping(target = "parentId",
            expression = "java(comment.getParentComment() != null ? comment.getParentComment().getId() : null)")
    @Mapping(target = "content", source = "comment.text")
    @Mapping(target = "author", expression = "java(com.cnytez.app.mapper.UserDisplayResolver.resolveAuthor(comment.getOwner()))")
    @Mapping(target = "score", expression = "java((int)(upvotes - downvotes))")
    @Mapping(target = "createdAt", source = "comment.creationDate")
    CommentDto toDto(Comment comment,
                            long upvotes,
                            long downvotes,
                            String userVote,
                            List<CommentDto> replies);

    VoteResponse toVoteResponse(CommentDto commentDto);
}