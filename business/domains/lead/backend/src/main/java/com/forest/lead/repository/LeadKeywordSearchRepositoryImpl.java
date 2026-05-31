package com.forest.lead.repository;

import com.forest.lead.entity.LeadPO;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 基于 PostgreSQL FTS + trigram 提供线索关键词搜索实现。
 */
@Repository
public class LeadKeywordSearchRepositoryImpl implements LeadKeywordSearchRepository {
    private static final double TRIGRAM_SCORE_WEIGHT = 0.35D;
    private static final String TRIGRAM_DOCUMENT_SQL =
        "lower(coalesce(l.name, '') || ' ' || coalesce(l.category, '') || ' ' || coalesce(l.keywords, ''))";
    private static final String SEARCH_CTE = """
        with params as (
            select
                websearch_to_tsquery('simple', :keyword) as tsquery,
                cast(:trigramKeyword as text) as trigram_keyword,
                cast(:trigramPattern as text) as trigram_pattern,
                cast(:trigramEnabled as boolean) as trigram_enabled
        ),
        scored as (
            select
                l.id,
                ts_rank_cd(l.search_vector, p.tsquery, 32) as fts_score,
                case
                    when p.trigram_enabled then word_similarity(p.trigram_keyword, %1$s)
                    else 0
                end as trigram_score,
                l.modified_time,
                l.created_time
            from lead l
            cross join params p
            where l.deleted = 0
              and (:country is null or l.country = :country)
              and (
                l.search_vector @@ p.tsquery
                or (p.trigram_enabled and %1$s like p.trigram_pattern)
              )
        )
        """.formatted(TRIGRAM_DOCUMENT_SQL);
    private static final String SEARCH_SQL = SEARCH_CTE + """
        select
            l.id,
            l.source_type,
            l.keywords,
            l.name,
            l.category,
            l.country,
            l.phone,
            l.email,
            l.website,
            l.intro,
            l.created_id,
            l.modified_id,
            l.deleted,
            l.created_time,
            l.modified_time
        from lead l
        join scored s on s.id = l.id
        order by (s.fts_score + s.trigram_score * :trigramWeight) desc,
                 s.fts_score desc,
                 s.modified_time desc,
                 s.created_time desc,
                 s.id desc
        limit :limit
        offset :offset
        """;
    private static final String COUNT_SQL = SEARCH_CTE + """
        select count(*)
        from scored
        """;
    private static final RowMapper<LeadPO> LEAD_ROW_MAPPER = (rs, rowNum) -> {
        LeadPO lead = new LeadPO();
        lead.setId(rs.getLong("id"));
        lead.setSourceType(rs.getString("source_type"));
        lead.setKeywords(rs.getString("keywords"));
        lead.setName(rs.getString("name"));
        lead.setCategory(rs.getString("category"));
        lead.setCountry(rs.getString("country"));
        lead.setPhone(rs.getString("phone"));
        lead.setEmail(rs.getString("email"));
        lead.setWebsite(rs.getString("website"));
        lead.setIntro(rs.getString("intro"));
        lead.setCreatedId(rs.getObject("created_id", Long.class));
        lead.setModifiedId(rs.getObject("modified_id", Long.class));
        lead.setDeleted(rs.getInt("deleted"));
        lead.setCreatedTime(rs.getObject("created_time", LocalDateTime.class));
        lead.setModifiedTime(rs.getObject("modified_time", LocalDateTime.class));
        return lead;
    };

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;
    private volatile Boolean nativeKeywordSearchSupported;

    public LeadKeywordSearchRepositoryImpl(
        NamedParameterJdbcTemplate namedParameterJdbcTemplate,
        DataSource dataSource
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public Page<LeadPO> searchByKeyword(String keyword, String country, Pageable pageable) {
        if (!supportsNativeKeywordSearch()) {
            throw new IllegalStateException("当前数据库不支持 PostgreSQL 原生线索搜索");
        }

        boolean trigramEnabled = keyword.length() >= 3;
        String normalizedTrigramKeyword = keyword.toLowerCase(Locale.ROOT);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("keyword", keyword, Types.VARCHAR)
            .addValue("country", country, Types.VARCHAR)
            .addValue("trigramKeyword", normalizedTrigramKeyword, Types.VARCHAR)
            .addValue("trigramPattern", trigramEnabled ? "%" + normalizedTrigramKeyword + "%" : "", Types.VARCHAR)
            .addValue("trigramEnabled", trigramEnabled, Types.BOOLEAN)
            .addValue("trigramWeight", TRIGRAM_SCORE_WEIGHT)
            .addValue("limit", pageable.getPageSize(), Types.INTEGER)
            .addValue("offset", pageable.getOffset(), Types.BIGINT);

        Long total = namedParameterJdbcTemplate.queryForObject(COUNT_SQL, parameters, Long.class);
        if (total == null || total == 0L) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<LeadPO> content = namedParameterJdbcTemplate.query(SEARCH_SQL, parameters, LEAD_ROW_MAPPER);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public boolean supportsNativeKeywordSearch() {
        Boolean cached = nativeKeywordSearchSupported;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (nativeKeywordSearchSupported != null) {
                return nativeKeywordSearchSupported;
            }
            try (java.sql.Connection connection = dataSource.getConnection()) {
                nativeKeywordSearchSupported = "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
            } catch (java.sql.SQLException ex) {
                throw new DataAccessException("无法识别数据库类型", ex) {
                };
            }
            return nativeKeywordSearchSupported;
        }
    }
}
