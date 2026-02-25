import { describe, expect, it } from "vitest";
import { buildQuery } from "@/lib/apiClient";

describe("buildQuery", () => {
  it("returns empty string when all params are nullish", () => {
    expect(buildQuery({ a: null, b: undefined })).toBe("");
  });

  it("builds query string with primitive values", () => {
    expect(buildQuery({ q: "laptop", page: 2, enabled: false })).toBe("?q=laptop&page=2&enabled=false");
  });
});
