export type CategoryMeta = {
  title: string;
  description: string;
  gradientClassName: string;
  icon: "bolt" | "book" | "shirt" | "laptop" | "phone" | "headphones" | "camera" | "tablet" | "sparkles" | "tag";
};

export const defaultCategoryMeta: CategoryMeta = {
  title: "Category",
  description: "Browse products in this category.",
  gradientClassName:
    "from-primary/25 via-background to-background",
  icon: "tag",
};

export const categoryMetaBySlug: Record<string, CategoryMeta> = {
  electronics: {
    title: "Electronics",
    description: "Phones, laptops, tablets, and accessories.",
    gradientClassName: "from-sky-500/25 via-background to-background",
    icon: "bolt",
  },
  books: {
    title: "Books",
    description: "Bestsellers, programming, and fiction picks.",
    gradientClassName: "from-amber-500/25 via-background to-background",
    icon: "book",
  },
  fashion: {
    title: "Fashion",
    description: "Fresh fits for every style and season.",
    gradientClassName: "from-pink-500/25 via-background to-background",
    icon: "shirt",
  },
  laptops: {
    title: "Laptops",
    description: "Work, gaming, and ultra‑portable machines.",
    gradientClassName: "from-indigo-500/25 via-background to-background",
    icon: "laptop",
  },
  smartphones: {
    title: "Smartphones",
    description: "Flagships and budget phones with great value.",
    gradientClassName: "from-violet-500/25 via-background to-background",
    icon: "phone",
  },
  tablets: {
    title: "Tablets",
    description: "For reading, sketching, and entertainment.",
    gradientClassName: "from-emerald-500/25 via-background to-background",
    icon: "tablet",
  },
  audio: {
    title: "Audio",
    description: "Headphones, speakers, and studio gear.",
    gradientClassName: "from-cyan-500/25 via-background to-background",
    icon: "headphones",
  },
  cameras: {
    title: "Cameras",
    description: "Capture moments with crisp details.",
    gradientClassName: "from-rose-500/25 via-background to-background",
    icon: "camera",
  },
  accessories: {
    title: "Accessories",
    description: "Chargers, cases, cables, and more.",
    gradientClassName: "from-teal-500/25 via-background to-background",
    icon: "sparkles",
  },
  "programming-books": {
    title: "Programming",
    description: "Level up with clean architecture & patterns.",
    gradientClassName: "from-fuchsia-500/25 via-background to-background",
    icon: "book",
  },
  "men-clothing": {
    title: "Men",
    description: "Everyday essentials and trending pieces.",
    gradientClassName: "from-blue-500/25 via-background to-background",
    icon: "shirt",
  },
  "women-clothing": {
    title: "Women",
    description: "Styles you’ll love—curated weekly.",
    gradientClassName: "from-pink-500/25 via-background to-background",
    icon: "shirt",
  },
  shoes: {
    title: "Shoes",
    description: "Run, walk, and flex in comfort.",
    gradientClassName: "from-orange-500/25 via-background to-background",
    icon: "sparkles",
  },
  skincare: {
    title: "Skincare",
    description: "Glow up with everyday routines.",
    gradientClassName: "from-lime-500/25 via-background to-background",
    icon: "sparkles",
  },
  fiction: {
    title: "Fiction",
    description: "Stories that keep you turning pages.",
    gradientClassName: "from-purple-500/25 via-background to-background",
    icon: "book",
  },
};

