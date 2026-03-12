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
    gradientClassName: "bg-gradient-to-br from-cyan-500/20 via-sky-500/15 to-white",
    icon: "bolt",
  },
  books: {
    title: "Books",
    description: "Bestsellers, programming, and fiction picks.",
    gradientClassName: "bg-gradient-to-br from-amber-400/25 via-orange-300/15 to-white",
    icon: "book",
  },
  fashion: {
    title: "Fashion",
    description: "Fresh fits for every style and season.",
    gradientClassName: "bg-gradient-to-br from-fuchsia-400/20 via-rose-300/10 to-white",
    icon: "shirt",
  },
  laptops: {
    title: "Laptops",
    description: "Work, gaming, and ultra-portable machines.",
    gradientClassName: "bg-gradient-to-br from-indigo-400/20 via-slate-300/15 to-white",
    icon: "laptop",
  },
  smartphones: {
    title: "Smartphones",
    description: "Flagships and budget phones with great value.",
    gradientClassName: "bg-gradient-to-br from-emerald-400/20 via-teal-300/10 to-white",
    icon: "phone",
  },
  tablets: {
    title: "Tablets",
    description: "For reading, sketching, and entertainment.",
    gradientClassName: "bg-gradient-to-br from-sky-400/20 via-blue-300/10 to-white",
    icon: "tablet",
  },
  audio: {
    title: "Audio",
    description: "Headphones, speakers, and studio gear.",
    gradientClassName: "bg-gradient-to-br from-violet-400/20 via-indigo-300/10 to-white",
    icon: "headphones",
  },
  cameras: {
    title: "Cameras",
    description: "Capture moments with crisp details.",
    gradientClassName: "bg-gradient-to-br from-slate-400/20 via-zinc-300/10 to-white",
    icon: "camera",
  },
  accessories: {
    title: "Accessories",
    description: "Chargers, cases, cables, and more.",
    gradientClassName: "bg-gradient-to-br from-orange-400/20 via-amber-300/10 to-white",
    icon: "sparkles",
  },
  "programming-books": {
    title: "Programming",
    description: "Level up with clean architecture & patterns.",
    gradientClassName: "bg-gradient-to-br from-cyan-500/20 via-sky-300/10 to-white",
    icon: "book",
  },
  "men-clothing": {
    title: "Men",
    description: "Everyday essentials and trending pieces.",
    gradientClassName: "bg-gradient-to-br from-blue-400/20 via-slate-300/10 to-white",
    icon: "shirt",
  },
  "women-clothing": {
    title: "Women",
    description: "Styles you'll love -- curated weekly.",
    gradientClassName: "bg-gradient-to-br from-pink-400/20 via-rose-300/10 to-white",
    icon: "shirt",
  },
  shoes: {
    title: "Shoes",
    description: "Run, walk, and flex in comfort.",
    gradientClassName: "bg-gradient-to-br from-lime-400/20 via-emerald-300/10 to-white",
    icon: "sparkles",
  },
  skincare: {
    title: "Skincare",
    description: "Glow up with everyday routines.",
    gradientClassName: "bg-gradient-to-br from-rose-400/20 via-orange-200/10 to-white",
    icon: "sparkles",
  },
  fiction: {
    title: "Fiction",
    description: "Stories that keep you turning pages.",
    gradientClassName: "bg-gradient-to-br from-amber-400/20 via-yellow-300/10 to-white",
    icon: "book",
  },
};
