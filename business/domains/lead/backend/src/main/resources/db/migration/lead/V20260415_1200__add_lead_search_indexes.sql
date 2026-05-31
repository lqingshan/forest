create extension if not exists pg_trgm;

alter table lead
    add column if not exists search_vector tsvector generated always as (
        setweight(to_tsvector('simple', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(keywords, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(category, '')), 'B')
    ) stored;

create index if not exists idx_lead_search_vector_active
    on lead using gin (search_vector)
    where deleted = 0;

create index if not exists idx_lead_trgm_document_active
    on lead using gin ((lower(coalesce(name, '') || ' ' || coalesce(category, '') || ' ' || coalesce(keywords, ''))) gin_trgm_ops)
    where deleted = 0;

create index if not exists idx_lead_country_active
    on lead (country)
    where deleted = 0 and country is not null;
