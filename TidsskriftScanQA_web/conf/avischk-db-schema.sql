create table batch
(
    batchid       varchar(255)      not null,
    avisid        varchar(255)      not null,
    roundtrip     integer           not null,
    start_date    date              not null,
    end_date      date              not null,
    delivery_date date              not null,
    problems      text,
    state         text              not null,
    num_problems  integer default 0 not null,
    username      varchar(255)      not null,
    lastmodified  timestamp         not null,

    CONSTRAINT unique_modstate UNIQUE (batchid, lastmodified)
);


CREATE TABLE newspaperarchive
(
    orig_relpath  VARCHAR(1024) PRIMARY KEY,
    format_type   VARCHAR(10)  NOT NULL,
    edition_date  DATE         NOT NULL,
    single_page   BOOLEAN      NOT NULL,
    page_number   INTEGER,
    avisid        VARCHAR(255) NOT NULL,
    avistitle     VARCHAR(255) NOT NULL,
    shadow_path   TEXT         NOT NULL,
    section_title VARCHAR(255),
    edition_title VARCHAR(255),
    delivery_date DATE         NOT NULL,
    handle        BIGSERIAL,
    side_label    VARCHAR(255),
    fraktur       BOOLEAN,
    problems      TEXT         NOT NULL,
    batchid       VARCHAR(255) NOT NULL,
    FOREIGN KEY (batchid) REFERENCES batch (batchid)
);



CREATE INDEX avisid_date_index ON newspaperarchive (avisid, edition_date);
CREATE INDEX avisid_format_index ON newspaperarchive (avisid, format_type);

CREATE TABLE notes
(
    id            SERIAL primary key unique not null,
    batchid       VARCHAR(255)              NOT NULL,
    avisid        VARCHAR(255)              NULL,
    edition_date  DATE                      NULL,
    edition_title VARCHAR(255)              NULL,
    section_title VARCHAR(255)              NULL,
    page_number   INTEGER                   NULL,
    username      VARCHAR(255)              NOT NULL,
    notes         TEXT                      NOT NULL,
    created       timestamptz               not null


--     FOREIGN KEY (batchid) REFERENCES batch (batchid)
-- TODO many to many links between avisIDs in notes and newspaperArchive
);
