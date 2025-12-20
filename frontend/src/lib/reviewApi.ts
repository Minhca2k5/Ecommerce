import { apiJson } from "@/lib/http";

export type ReviewResponse = {
  id?: number;
  createdAt?: string;
  rating?: number;
  comment?: string;
  productId?: number;
  productName?: string;
  productSlug?: string;
  userId?: number;
  username?: string;
  fullName?: string;
};

export type ReviewRequest = {
  productId: number;
  rating: number;
  comment?: string;
};

export function listMyReviews() {
  return apiJson<ReviewResponse[]>("/api/users/me/reviews", { method: "GET", auth: true });
}

export function createMyReview(request: ReviewRequest) {
  return apiJson<ReviewResponse>("/api/users/me/reviews", { method: "POST", auth: true, body: request });
}

export function updateMyReviewRating(reviewId: number, rating: number) {
  return apiJson<void>(`/api/users/me/reviews/${reviewId}/rating?rating=${encodeURIComponent(String(rating))}`, { method: "PATCH", auth: true });
}

export function updateMyReviewComment(reviewId: number, comment: string) {
  return apiJson<void>(`/api/users/me/reviews/${reviewId}/comment?comment=${encodeURIComponent(comment)}`, { method: "PATCH", auth: true });
}

export function deleteMyReview(reviewId: number) {
  return apiJson<void>(`/api/users/me/reviews/${reviewId}`, { method: "DELETE", auth: true });
}

