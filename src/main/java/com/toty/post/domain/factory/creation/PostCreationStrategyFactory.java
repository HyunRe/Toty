package com.toty.post.domain.factory.creation;

import com.toty.post.domain.strategy.creation.PostCreationStrategy;
import com.toty.post.domain.model.post.PostCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PostCreationStrategyFactory {
    private final Map<PostCategory, PostCreationStrategy> strategyMap = new HashMap<>();

    @Autowired
    public PostCreationStrategyFactory(List<PostCreationStrategy> strategies) {
        for (PostCreationStrategy strategy : strategies) {
            strategyMap.put(strategy.getPostCategory(), strategy);
        }
    }

    public PostCreationStrategy getStrategy(PostCategory category) {
        return strategyMap.get(category);
    }
}