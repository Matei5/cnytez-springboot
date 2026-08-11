package com.cnytez.app.mapper;

import com.cnytez.app.dto.internal.PostDto;
import com.cnytez.app.dto.response.VoteResponse;
import com.cnytez.app.model.Post;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PostMapper {
    @Mapping(target = "content", source = "post.text")
    @Mapping(target = "imageUrl", source = "post.image")
    @Mapping(target = "filter", source = "filterId")
    @Mapping(target = "author", expression = "java(com.cnytez.app.mapper.UserDisplayResolver.resolveAuthor(post.getOwner()))")
    @Mapping(target = "subreddit", expression = "java(post.getSubreddit().getName())")
    @Mapping(target = "score", expression = "java((int)(upvotes - downvotes))")
    @Mapping(target = "createdAt", source = "post.creationDate")
    PostDto toDto(Post post,
                         long upvotes,
                         long downvotes,
                         long commentCount,
                         String userVote,
                         Integer filterId);

    VoteResponse toVoteResponse(PostDto postDto);
}
