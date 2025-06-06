package com.model;

public class Review {
    private final String reviewerUsername;
    private final String comment;
    private final int rating; // e.g., 1–5 stars

    public Review(String reviewerUsername, String comment, int rating) {
        this.reviewerUsername = reviewerUsername;
        this.comment = comment;
        this.rating = rating;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return reviewerUsername + " (" + rating + "/5): " + comment;
    }
}
