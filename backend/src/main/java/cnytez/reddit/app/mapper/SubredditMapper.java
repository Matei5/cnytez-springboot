package cnytez.reddit.app.mapper;

import cnytez.reddit.app.dto.SubredditDto;
import cnytez.reddit.app.model.Subreddit;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SubredditMapper {
    @Mapping(target = "memberCount", expression = "java(subreddit.getMembers().size())")
    @Mapping(target = "postCount", source = "postCount")
    @Mapping(target = "createdAt", source = "subreddit.creationDate")
    SubredditDto toDto(Subreddit subreddit, long postCount);
}
