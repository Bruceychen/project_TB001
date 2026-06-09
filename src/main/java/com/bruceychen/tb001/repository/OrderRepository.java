package com.bruceychen.tb001.repository;

import com.bruceychen.tb001.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Order repository.
 *
 * <p><b>背景：為什麼這裡要處理「一次撈完關聯」</b><br>
 * {@code Order} 對 {@code user} 與 {@code product} 都是 {@code @ManyToOne(fetch = LAZY)}，
 * 而 {@code Product} 對 {@code category} 也是 LAZY。Service 的 {@code toResponse()} 會去讀
 * {@code order.getUser()}、{@code order.getProduct()}、{@code product.getCategory()}。
 * 若用最樸素的衍生查詢一筆筆讀，這些 LAZY 關聯會在迴圈裡各觸發一條 SELECT——典型的
 * <b>N+1 問題</b>。
 *
 * <p>底下提供「同一件事的兩種寫法」，效果等價、風格不同，面試常被要求比較：
 * <ol>
 *   <li>{@link #findByUser_UserId(Long)} —— <b>@EntityGraph（宣告式）</b></li>
 *   <li>{@link #findWithDetailsByUserId(Long)} —— <b>@Query JPQL join fetch（明確式）</b></li>
 * </ol>
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 寫法一：@EntityGraph（宣告式 fetch）。
     *
     * <p>方法名沿用 Spring Data 衍生查詢規則（{@code findByUser_UserId} 解析成
     * {@code where user.user_id = ?}），再用 {@code @EntityGraph} 告訴 Hibernate：
     * 「執行這個查詢時，順便把 user / product / product.category 一起 join 進來」。
     *
     * <p>優點：不必手寫 JPQL，只宣告「要哪些關聯」、和方法名解耦。
     * <p>備註：原本只列 {@code product, product.category}，這裡補上 {@code user}，
     * 讓三個 to-one 關聯全在同一條 SQL 撈完、達到零 lazy 載入（一致性）。
     */
    @EntityGraph(attributePaths = {"user", "product", "product.category"})
    List<Order> findByUser_UserId(Long userId);

    /**
     * 寫法二：@Query + JPQL {@code join fetch}（明確式）。
     *
     * <p>和寫法一效果相同，但把「要 join 哪些、條件是什麼」完全寫明。三個都是 to-one
     * 關聯，{@code join fetch} 不會產生笛卡兒重複，所以不需要 {@code distinct}。
     *
     * <p>優點：查詢意圖一目了然、可加複雜條件/排序、複雜情境比 @EntityGraph 好掌控。
     * <p>取捨：JPQL 操作 entity（跨 DB 可攜）；若要用 DB 專屬特性或極致調校，可改成
     * {@code @Query(value = "...原生 SQL...", nativeQuery = true)}（彈性高但綁特定 DB）。
     */
    @Query("""
            select o
            from Order o
            join fetch o.user
            join fetch o.product p
            join fetch p.category
            where o.user.userId = :userId
            """)
    List<Order> findWithDetailsByUserId(@Param("userId") Long userId);
}
