package com.example.findex.domain.Index_Info.entity;

import com.example.findex.common.base.BaseEntity;
import com.example.findex.common.base.SourceType;
import com.example.findex.domain.Auto_Sync.entity.AutoSync;
import com.example.findex.domain.Index_Info.dto.IndexInfoCreateRequest;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "index_info",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_index_classification_name",
                        columnNames = {"index_classification", "index_name"}
                )
        }
)
public class IndexInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "index_classification", nullable = false, length = 100)
    private String indexClassification; // 지수 분류명

    @Setter
    @Column(name = "index_name", nullable = false, length = 255)
    private String indexName; // 지수 이름

    @Setter
    @Column(name = "employed_items_count")
    private int employedItemsCount; // 채용 종목 수

    @Setter
    @Column(name = "base_point_in_time")
    private LocalDate basePointInTime; // 기준 시점

    @Setter
    @Column(name = "base_index", precision = 18, scale = 2)
    private BigDecimal baseIndex; // 기준 지수

    @Column(name = "source_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType; // 소스 타입 (USER, OpenAPI)

    @Setter
    @Column(name = "favorite", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean favorite; // 즐겨찾기 (기본값 : false)

    @OneToOne(mappedBy = "indexInfo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private AutoSync autoSync;

    ///     == 연관관계 설정 메서드 ==   ///
    public void setAutoSync(AutoSync autoSync) {
        this.autoSync = autoSync;
    }

}
