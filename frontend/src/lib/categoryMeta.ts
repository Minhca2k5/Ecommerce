export type CategoryMeta = {
  title: string;
  description: string;
  gradientClassName: string;
  icon: "bolt" | "book" | "shirt" | "laptop" | "phone" | "headphones" | "camera" | "tablet" | "sparkles" | "tag";
};

export const defaultCategoryMeta: CategoryMeta = {
  title: "Category",
  description: "Browse products in this category.",
  gradientClassName: "bg-muted",
  icon: "tag",
};

export const categoryMetaBySlug: Record<string, CategoryMeta> = {
  electronics: {
    title: "Electronics",
    description: "Phones, laptops, tablets, and accessories.",
    gradientClassName: "bg-muted",
    icon: "bolt",
  },
  books: {
    title: "Books",
    description: "Bestsellers, programming, and fiction picks.",
    gradientClassName: "bg-muted",
    icon: "book",
  },
  fashion: {
    title: "Fashion",
    description: "Fresh fits for every style and season.",
    gradientClassName: "bg-muted",
    icon: "shirt",
  },
  laptops: {
    title: "Laptops",
    description: "Work, gaming, and ultra‑portable machines.",
    gradientClassName: "bg-muted",
    icon: "laptop",
  },
  smartphones: {
    title: "Smartphones",
    description: "Flagships and budget phones with great value.",
    gradientClassName: "bg-muted",
    icon: "phone",
  },
  tablets: {
    title: "Tablets",
    description: "For reading, sketching, and entertainment.",
    gradientClassName: "bg-muted",
    icon: "tablet",
  },
  audio: {
    title: "Audio",
    description: "Headphones, speakers, and studio gear.",
    gradientClassName: "bg-muted",
    icon: "headphones",
  },
  cameras: {
    title: "Cameras",
    description: "Capture moments with crisp details.",
    gradientClassName: "bg-muted",
    icon: "camera",
  },
  accessories: {
    title: "Accessories",
    description: "Chargers, cases, cables, and more.",
    gradientClassName: "bg-muted",
    icon: "sparkles",
  },
  "programming-books": {
    title: "Programming",
    description: "Level up with clean architecture & patterns.",
    gradientClassName: "bg-muted",
    icon: "book",
  },
  "men-clothing": {
    title: "Men",
    description: "Everyday essentials and trending pieces.",
    gradientClassName: "bg-muted",
    icon: "shirt",
  },
  "women-clothing": {
    title: "Women",
    description: "Styles you’ll love—curated weekly.",
    gradientClassName: "bg-muted",
    icon: "shirt",
  },
  shoes: {
    title: "Shoes",
    description: "Run, walk, and flex in comfort.",
    gradientClassName: "bg-muted",
    icon: "sparkles",
  },
  skincare: {
    title: "Skincare",
    description: "Glow up with everyday routines.",
    gradientClassName: "bg-muted",
    icon: "sparkles",
  },
  fiction: {
    title: "Fiction",
    description: "Stories that keep you turning pages.",
    gradientClassName: "bg-muted",
    icon: "book",
  },
};
