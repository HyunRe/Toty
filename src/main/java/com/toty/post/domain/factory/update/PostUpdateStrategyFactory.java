package com.toty.post.domain.factory.update;

import com.toty.post.domain.strategy.update.PostUpdateStrategy;
import com.toty.post.domain.model.post.PostCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PostUpdateStrategyFactory {
    private final Map<PostCategory, PostUpdateStrategy> strategyMap = new HashMap<>();

    @Autowired
    public PostUpdateStrategyFactory(List<PostUpdateStrategy> strategies) {
        for (PostUpdateStrategy strategy : strategies) {
            strategyMap.put(strategy.getPostCategory(), strategy);
        }
    }

    public PostUpdateStrategy getStrategy(PostCategory category) {
        return strategyMap.get(category);
    }
}