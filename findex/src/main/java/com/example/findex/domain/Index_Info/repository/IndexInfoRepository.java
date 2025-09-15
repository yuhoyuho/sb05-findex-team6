package com.example.findex.domain.Index_Info.repository;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>, IndexInfoRepositoryCustom {

    // 지수 분류명과 지수명으로 IndexInfo를 찾는 메서드
    Optional<IndexInfo> findByIndexNameAndIndexClassification(String indexName, String indexClassification);

    // 처음 조회
    List<IndexInfo> findAllByOrderByIdAsc(Pageable pageable);

    // 주어진 cursor(id)보다 큰 데이터 조회 (커서 페이지네이션)
    List<IndexInfo> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    // 정렬 조건을 Pageable로 전달받는 버전 (asc/desc 동적으로 적용 가능)
    List<IndexInfo> findByIdGreaterThan(Long id,Pageable pageable);

    @Query("SELECT i FROM IndexInfo i LEFT JOIN FETCH i.autoSync")
    List<IndexInfo> findAllWithAutoSync();

    @Query("""
    select count(i)
    from IndexInfo i
    where (:cls = '' or i.indexClassification like concat('%', :cls, '%'))
      and (:name = '' or i.indexName like concat('%', :name, '%'))
      and (:fav is null or i.favorite = :fav)
""")
    long countByFilter(String cls, String name, Boolean fav);

}
