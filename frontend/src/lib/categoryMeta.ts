export type CategoryMeta = {
  title: string;
  description: string;
  gradientClassName: string;
  imageUrl: string;
  accentClassName: string;
  badge?: string;
  icon: "bolt" | "book" | "shirt" | "laptop" | "phone" | "headphones" | "camera" | "tablet" | "sparkles" | "tag";
};

export const defaultCategoryMeta: CategoryMeta = {
  title: "Category",
  description: "Shop bestsellers in this category.",
  gradientClassName: "bg-muted",
  imageUrl: "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=800&q=80",
  accentClassName: "border-slate-200 bg-slate-100 text-slate-700",
  icon: "tag",
};

export const categoryMetaBySlug: Record<string, CategoryMeta> = {
  electronics: {
    title: "Electronics",
    description: "Phones, laptops, and smart gear.",
    gradientClassName: "bg-gradient-to-br from-cyan-500/20 via-sky-500/15 to-white",
    imageUrl: "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-sky-200 bg-sky-100 text-sky-800",
    icon: "bolt",
  },
  books: {
    title: "Books",
    description: "Bestsellers and binge-worthy reads.",
    gradientClassName: "bg-gradient-to-br from-amber-400/25 via-orange-300/15 to-white",
    imageUrl: "https://images.unsplash.com/photo-1519682577862-22b62b24e493?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-amber-200 bg-amber-100 text-amber-800",
    icon: "book",
  },
  fashion: {
    title: "Fashion",
    description: "Fresh fits, updated weekly.",
    gradientClassName: "bg-gradient-to-br from-fuchsia-400/20 via-rose-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=900&q=80",
    accentClassName: "border-rose-200 bg-rose-100 text-rose-800",
    badge: "Trending",
    icon: "shirt",
  },
  laptops: {
    title: "Laptops",
    description: "Work, gaming, and ultra-portables.",
    gradientClassName: "bg-gradient-to-br from-indigo-400/20 via-slate-300/15 to-white",
    imageUrl: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-indigo-200 bg-indigo-100 text-indigo-800",
    icon: "laptop",
  },
  smartphones: {
    title: "Smartphones",
    description: "Flagships and value picks.",
    gradientClassName: "bg-gradient-to-br from-emerald-400/20 via-teal-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-emerald-200 bg-emerald-100 text-emerald-800",
    icon: "phone",
  },
  tablets: {
    title: "Tablets",
    description: "Work, sketch, and stream.",
    gradientClassName: "bg-gradient-to-br from-sky-400/20 via-blue-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-blue-200 bg-blue-100 text-blue-800",
    icon: "tablet",
  },
  audio: {
    title: "Audio",
    description: "Headphones, speakers, and more.",
    gradientClassName: "bg-gradient-to-br from-violet-400/20 via-indigo-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1511379938547-c1f69419868d?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-violet-200 bg-violet-100 text-violet-800",
    icon: "headphones",
  },
  cameras: {
    title: "Cameras",
    description: "Capture sharp, share-ready shots.",
    gradientClassName: "bg-gradient-to-br from-slate-400/20 via-zinc-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-slate-200 bg-slate-100 text-slate-700",
    icon: "camera",
  },
  accessories: {
    title: "Accessories",
    description: "Chargers, cases, and extras.",
    gradientClassName: "bg-gradient-to-br from-orange-400/20 via-amber-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1511367461989-f85a21fda167?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-orange-200 bg-orange-100 text-orange-800",
    icon: "sparkles",
  },
  "programming-books": {
    title: "Programming",
    description: "Level up your stack.",
    gradientClassName: "bg-gradient-to-br from-cyan-500/20 via-sky-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-cyan-200 bg-cyan-100 text-cyan-800",
    icon: "book",
  },
  "men-clothing": {
    title: "Men",
    description: "Essentials and fresh drops.",
    gradientClassName: "bg-gradient-to-br from-blue-400/20 via-slate-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-blue-200 bg-blue-100 text-blue-800",
    icon: "shirt",
  },
  "women-clothing": {
    title: "Women",
    description: "Styles you will love.",
    gradientClassName: "bg-gradient-to-br from-pink-400/20 via-rose-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-pink-200 bg-pink-100 text-pink-800",
    icon: "shirt",
  },
  shoes: {
    title: "Shoes",
    description: "Comfort meets style.",
    gradientClassName: "bg-gradient-to-br from-lime-400/20 via-emerald-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-lime-200 bg-lime-100 text-lime-800",
    icon: "sparkles",
  },
  skincare: {
    title: "Skincare",
    description: "Everyday glow essentials.",
    gradientClassName: "bg-gradient-to-br from-rose-400/20 via-orange-200/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-rose-200 bg-rose-100 text-rose-800",
    icon: "sparkles",
  },
  fiction: {
    title: "Fiction",
    description: "Stories you cannot put down.",
    gradientClassName: "bg-gradient-to-br from-amber-400/20 via-yellow-300/10 to-white",
    imageUrl: "https://images.unsplash.com/photo-1474932430478-367dbb6832c1?auto=format&fit=crop&w=800&q=80",
    accentClassName: "border-yellow-200 bg-yellow-100 text-yellow-800",
    icon: "book",
  },
};
