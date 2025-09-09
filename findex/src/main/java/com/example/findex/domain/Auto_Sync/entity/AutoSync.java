package com.example.findex.domain.Auto_Sync.entity;

import com.example.findex.common.base.BaseEntity;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // ✨ 무분별한 객체 생성을 막기 위해 접근 수준을 PROTECTED로 설정
@AllArgsConstructor
@SuperBuilder
@Table(name = "auto_sync_config")
public class AutoSync extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id", nullable = false, unique = true)
    private IndexInfo indexInfo; // 지수 정보

    @Column(name = "enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean enabled;

    ///     == 비즈니스 로직 ==   ///

    /**
     * 활성화 상태 수정 메서드
     */
    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
