-- Adminer 4.6.3 PostgreSQL dump

DROP TABLE IF EXISTS "emoji";
CREATE TABLE "public"."emoji" (
    "code" character varying(10) NOT NULL,
    "category" character varying(32) NOT NULL,
    "name" character varying(64) NOT NULL,
    "usage_count" integer DEFAULT '0',
    CONSTRAINT "emoji_code" UNIQUE ("code")
) WITH (oids = false);


DROP TABLE IF EXISTS "emoji_usage";
CREATE TABLE "public"."emoji_usage" (
    "code" character varying(10) NOT NULL,
    "used_at" timestamptz NOT NULL,
    CONSTRAINT "emoji_usage_code_used_at" UNIQUE ("code", "used_at"),
    CONSTRAINT "emoji_usage_code_fkey" FOREIGN KEY (code) REFERENCES emoji(code) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE
) WITH (oids = false);


-- 2018-10-29 18:41:23.018344+00
