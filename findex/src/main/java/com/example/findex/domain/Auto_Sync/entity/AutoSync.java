package com.example.findex.domain.Auto_Sync.entity;

import com.example.findex.common.base.BaseEntity;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(name = "auto_integration_config")
public class AutoSync extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id",
            foreignKey = @ForeignKey(name = "fk_index_info_to_config"),
            nullable = false, unique = true)
    private IndexInfo indexInfo;

    @Column(nullable = false)
    private boolean enabled;

    ///     == 비즈니스 로직 ==   ///

    /**
     * 활성화 상태 수정 메서드
     */
    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
