package org.project.shop.service;

import org.project.shop.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    public void save(Review review);

    public Review findOneReview(Long reviewId);

    public List<Review> findAllReviewByItemId(Long ItemId);

    public Page<Review> findPageReviewByItemId(PageRequest pageRequest, Long itemId);

    public List<Review> findAllReviewByMemberId(Long memberId);

    public Optional<Review> findOneReviewByItemIdAndMemberId(Long itemId, Long memberId);

    public void deleteReview(Long reviewId);

    public void deleteAll();

    public List<Review> findAllReview();
}
