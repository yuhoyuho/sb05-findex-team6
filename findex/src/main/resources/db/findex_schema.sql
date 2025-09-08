DROP TABLE IF EXISTS index_info CASCADE;
DROP TABLE IF EXISTS index_data CASCADE;
DROP TABLE IF EXISTS auto_integration_config CASCADE;
DROP TABLE IF EXISTS integration_job CASCADE;

-- 지수 정보
CREATE TABLE index_info (
    id BIGSERIAL PRIMARY KEY,
    index_classification VARCHAR(100) NOT NULL,
    index_name VARCHAR(255) NOT NULL,
    employed_items_count INT NULL,
    base_point_in_time DATE NULL,
    base_index NUMERIC(18, 2) NULL,
    source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
    favorite BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (index_classification, index_name)
);

-- 지수 데이터
CREATE TABLE index_data (
    id BIGSERIAL PRIMARY KEY,
    index_info_id BIGINT NOT NULL,
    base_date DATE NOT NULL,
    source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
    market_price NUMERIC(18, 2) NULL,
    closing_price NUMERIC(18, 2) NULL,
    high_price NUMERIC(18, 2) NULL,
    low_price NUMERIC(18, 2) NULL,
    versus NUMERIC(18, 2) NULL,
    fluctuation_rate NUMERIC(5, 2) NULL,
    trading_quantity BIGINT NULL,
    trading_price BIGINT NULL,
    market_total_amount BIGINT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (index_info_id, base_date),
    CONSTRAINT fk_index_info_to_data FOREIGN KEY (index_info_id)
        REFERENCES index_info (id)
);

-- 자동 연동 설정
CREATE TABLE auto_integration_config (
    id BIGSERIAL PRIMARY KEY,
    index_info_id BIGINT NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_index_info_to_config FOREIGN KEY (index_info_id)
        REFERENCES index_info (id)
);

-- 연동 관리
CREATE TABLE integration_job (
    id BIGSERIAL PRIMARY KEY,
    index_info_id BIGINT NOT NULL,
    job_type VARCHAR(20) NOT NULL CHECK (job_type IN ('INDEX_INFO', 'INDEX_DATA')),
    target_date DATE NULL,
    worker VARCHAR(255) NOT NULL,
    job_time TIMESTAMP NOT NULL,
    result VARCHAR(20) NOT NULL CHECK (result IN ('SUCCESS', 'FAILURE')),
    details TEXT NULL,
    CONSTRAINT fk_index_info_to_job FOREIGN KEY (index_info_id)
        REFERENCES index_info (id)
);