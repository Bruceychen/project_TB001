# Assumptions & Open Questions

Captures the interpretive decisions I made while implementing the assignment, and the points I would normally have asked the PM / spec owner about before starting. The intention is to make the design decision trail visible to the reviewer.

---

## 1. ER diagram strictness

**Spec wording:** "Reference ER diagram"

**My interpretation:** the ER is binding on **structure** (4 tables, the columns shown, the foreign-key relationships), but I followed standard JVM/Java conventions for naming (snake_case columns, lowercase plural table for `orders`). The `UserID` shown in PascalCase on the ER is implemented as the standard `user_id` column.

**Alternative interpretation:** ER is binding literally on column case. Under that reading, `@Column(name = "\"UserID\"")` (Postgres-quoted, case-preserving) would be required. I judged that overly literal; the rest of the columns in the ER use snake_case so the PascalCase on `UserID` reads to me as diagram styling, not a literal requirement.

**Why I'd normally ask:** schema naming is the kind of thing that ripples across migrations, ORM mappings, BI tooling, and any other system referencing the same tables. Lock it down before code is written.

---

## 2. `Order` table name

`order` is a SQL reserved word in PostgreSQL (and most dialects). Implementing the entity as `@Table(name = "orders")` is intentional — it avoids Hibernate having to quote the identifier on every generated query.

If the ER name `Order` is binding literally, the alternative is `@Table(name = "\"Order\"")` plus quoted references in every query, which I considered unnecessary risk.

---

## 3. PATCH `/api/order/{order_id}` field scope

**My choice:** allow `productId` and `orderAmount` to be patched. `userId` is intentionally **not** in the patch DTO.

**Reasoning:**
- A customer changing their mind about product or quantity is a normal business flow.
- Changing the *owner* of an order is not — it would cleanly bypass any audit trail of who placed it. If a transfer is needed, it should be a different operation (cancel + recreate, or an explicit `transfer` endpoint with its own authorisation).

If the assignment expects `userId` to also be patchable, the change is one extra field in `OrderPatchRequest` and one more `setUser` call in `OrderService.patch`.

---

## 4. GET `/api/order/{userId}` behaviour when user does not exist

**My choice:** return `404 Not Found` (via `ResourceNotFoundException`).

**Alternative:** return `200 OK` with an empty array.

I went with 404 because it distinguishes "user does not exist" from "user exists but has placed no orders" — different errors, different fixes, different signals. An empty 200 collapses both cases.

If the spec prefers the empty-list semantics, the change is removing the `existsById` check in `OrderService.findByUserId`.

---

## 5. Scope-creep supporting endpoints

The spec defines six endpoints. The project includes a few extra ones (`/api/user`, `/api/product`, `/api/category` list / get) so that the API is self-sufficient for populating, inspecting, and resetting state during testing — without needing direct SQL access.

If strict-spec compliance is preferred (only the six endpoints), the supporting endpoints are easy to remove cleanly: delete the corresponding methods in `UserController`, `ProductController`, `ProductCategoryController`, plus the no-longer-used methods on `UserService`.

---

## 6. Submission format

I haven't been told whether you prefer a ZIP, a private GitHub repo, or a different delivery. This package assumes ZIP-or-repo would both work. If you have a preferred format, let me know and I'll repackage.

---
