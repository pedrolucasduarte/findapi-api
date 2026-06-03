CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE TABLE app_users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(120) NOT NULL,
    email varchar(320) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role varchar(30) NOT NULL DEFAULT 'USER',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT chk_app_users_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_app_users_email_basic CHECK (position('@' IN email) > 1),
    CONSTRAINT chk_app_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE authentication_methods (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(40) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT chk_authentication_methods_name CHECK (
        name IN ('NONE', 'API_KEY', 'BEARER', 'OAUTH2', 'BASIC_AUTH', 'HMAC')
    )
);

CREATE TABLE categories (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(100) NOT NULL,
    slug varchar(120) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT chk_categories_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_categories_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);

CREATE TABLE tags (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(80) NOT NULL,
    slug varchar(100) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT chk_tags_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_tags_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);

CREATE TABLE apis (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    authentication_method_id uuid NOT NULL,
    name varchar(160) NOT NULL,
    slug varchar(180) NOT NULL,
    short_description varchar(280) NOT NULL,
    full_description text,
    official_site varchar(2048),
    documentation_url varchar(2048),
    api_type varchar(30) NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'ACTIVE',
    free_tier boolean NOT NULL DEFAULT false,
    official_sdk boolean NOT NULL DEFAULT false,
    open_source boolean NOT NULL DEFAULT false,
    self_hosted boolean NOT NULL DEFAULT false,
    brazilian boolean NOT NULL DEFAULT false,
    integration_difficulty varchar(30) NOT NULL DEFAULT 'MEDIUM',
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(short_description, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(full_description, '')), 'C')
    ) STORED,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT fk_apis_authentication_method FOREIGN KEY (authentication_method_id)
        REFERENCES authentication_methods (id),
    CONSTRAINT chk_apis_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_apis_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT chk_apis_short_description_not_blank CHECK (btrim(short_description) <> ''),
    CONSTRAINT chk_apis_api_type CHECK (api_type IN ('PUBLIC', 'FREEMIUM', 'PAID', 'OPEN_SOURCE')),
    CONSTRAINT chk_apis_status CHECK (status IN ('ACTIVE', 'BETA', 'DEPRECATED', 'DISCONTINUED')),
    CONSTRAINT chk_apis_integration_difficulty CHECK (integration_difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT chk_apis_official_site_url CHECK (
        official_site IS NULL OR official_site ~* '^https?://'
    ),
    CONSTRAINT chk_apis_documentation_url CHECK (
        documentation_url IS NULL OR documentation_url ~* '^https?://'
    )
);

CREATE TABLE api_categories (
    api_id uuid NOT NULL,
    category_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT pk_api_categories PRIMARY KEY (api_id, category_id),
    CONSTRAINT fk_api_categories_api FOREIGN KEY (api_id)
        REFERENCES apis (id) ON DELETE CASCADE,
    CONSTRAINT fk_api_categories_category FOREIGN KEY (category_id)
        REFERENCES categories (id) ON DELETE CASCADE
);

CREATE TABLE api_tags (
    api_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT pk_api_tags PRIMARY KEY (api_id, tag_id),
    CONSTRAINT fk_api_tags_api FOREIGN KEY (api_id)
        REFERENCES apis (id) ON DELETE CASCADE,
    CONSTRAINT fk_api_tags_tag FOREIGN KEY (tag_id)
        REFERENCES tags (id) ON DELETE CASCADE
);

CREATE TABLE pricing_plans (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    api_id uuid NOT NULL,
    name varchar(120) NOT NULL,
    billing_type varchar(30) NOT NULL,
    free_limit varchar(120),
    price numeric(12, 2),
    currency char(3),
    description text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT fk_pricing_plans_api FOREIGN KEY (api_id)
        REFERENCES apis (id) ON DELETE CASCADE,
    CONSTRAINT chk_pricing_plans_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_pricing_plans_billing_type CHECK (
        billing_type IN ('FREE', 'USAGE_BASED', 'SUBSCRIPTION', 'ONE_TIME', 'CUSTOM')
    ),
    CONSTRAINT chk_pricing_plans_price_non_negative CHECK (price IS NULL OR price >= 0),
    CONSTRAINT chk_pricing_plans_currency_format CHECK (
        currency IS NULL OR currency ~ '^[A-Z]{3}$'
    )
);

CREATE TABLE code_examples (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    api_id uuid NOT NULL,
    language varchar(60) NOT NULL,
    title varchar(160) NOT NULL,
    description text,
    code text NOT NULL,
    version varchar(60),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT fk_code_examples_api FOREIGN KEY (api_id)
        REFERENCES apis (id) ON DELETE CASCADE,
    CONSTRAINT chk_code_examples_language_not_blank CHECK (btrim(language) <> ''),
    CONSTRAINT chk_code_examples_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT chk_code_examples_code_not_blank CHECK (btrim(code) <> '')
);

CREATE TABLE reviews (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    api_id uuid NOT NULL,
    user_id uuid NOT NULL,
    rating smallint NOT NULL,
    comment text,
    stack_used varchar(160),
    integration_difficulty varchar(30),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT fk_reviews_api FOREIGN KEY (api_id)
        REFERENCES apis (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id)
        REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_reviews_integration_difficulty CHECK (
        integration_difficulty IS NULL OR integration_difficulty IN ('EASY', 'MEDIUM', 'HARD')
    )
);

CREATE TABLE collections (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    name varchar(120) NOT NULL,
    slug varchar(140) NOT NULL,
    description text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT fk_collections_user FOREIGN KEY (user_id)
        REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT chk_collections_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_collections_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);

CREATE TABLE collection_apis (
    collection_id uuid NOT NULL,
    api_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT pk_collection_apis PRIMARY KEY (collection_id, api_id),
    CONSTRAINT fk_collection_apis_collection FOREIGN KEY (collection_id)
        REFERENCES collections (id) ON DELETE CASCADE,
    CONSTRAINT fk_collection_apis_api FOREIGN KEY (api_id)
        REFERENCES apis (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX ux_app_users_email_active
    ON app_users (lower(email))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_authentication_methods_name_active
    ON authentication_methods (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_categories_slug_active
    ON categories (slug)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_tags_slug_active
    ON tags (slug)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_apis_slug_active
    ON apis (slug)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_reviews_api_user_active
    ON reviews (api_id, user_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_collections_user_slug_active
    ON collections (user_id, slug)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_apis_authentication_method_id ON apis (authentication_method_id);
CREATE INDEX ix_apis_api_type ON apis (api_type) WHERE deleted_at IS NULL;
CREATE INDEX ix_apis_status ON apis (status) WHERE deleted_at IS NULL;
CREATE INDEX ix_apis_flags ON apis (free_tier, open_source, self_hosted, brazilian) WHERE deleted_at IS NULL;
CREATE INDEX ix_apis_integration_difficulty ON apis (integration_difficulty) WHERE deleted_at IS NULL;
CREATE INDEX ix_apis_search_vector ON apis USING gin (search_vector);
CREATE INDEX ix_apis_name_trgm ON apis USING gin (name gin_trgm_ops) WHERE deleted_at IS NULL;

CREATE INDEX ix_api_categories_category_id ON api_categories (category_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_api_tags_tag_id ON api_tags (tag_id) WHERE deleted_at IS NULL;

CREATE INDEX ix_pricing_plans_api_id ON pricing_plans (api_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_code_examples_api_id ON code_examples (api_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_reviews_api_id ON reviews (api_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_reviews_user_id ON reviews (user_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_reviews_rating ON reviews (rating) WHERE deleted_at IS NULL;
CREATE INDEX ix_collections_user_id ON collections (user_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_collection_apis_api_id ON collection_apis (api_id) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_app_users_set_updated_at
    BEFORE UPDATE ON app_users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_authentication_methods_set_updated_at
    BEFORE UPDATE ON authentication_methods
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_categories_set_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_tags_set_updated_at
    BEFORE UPDATE ON tags
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_apis_set_updated_at
    BEFORE UPDATE ON apis
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_pricing_plans_set_updated_at
    BEFORE UPDATE ON pricing_plans
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_code_examples_set_updated_at
    BEFORE UPDATE ON code_examples
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_reviews_set_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_collections_set_updated_at
    BEFORE UPDATE ON collections
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
