

CREATE TABLE newspaperarchive (
	orig_relpath VARCHAR(1024) PRIMARY KEY,
	format_type VARCHAR(10) NOT NULL,
	edition_date DATE NOT NULL,
	single_page BOOLEAN NOT NULL,
	page_number INTEGER,
	avisid VARCHAR(255) NOT NULL,
	avistitle VARCHAR(255) NOT NULL,
	shadow_path TEXT NOT NULL,
	section_title VARCHAR(255),
	edition_title VARCHAR(255),
	delivery_date DATE NOT NULL,
	handle BIGSERIAL,
	side_label VARCHAR(255),
	fraktur BOOLEAN
);

CREATE INDEX avisid_date_index ON newspaperarchive(avisid, edition_date);
CREATE INDEX avisid_format_index ON newspaperarchive(avisid, format_type);


CREATE TABLE characterisation_info (
	orig_relpath VARCHAR(1024) NOT NULL,
	tool VARCHAR(100),
	characterisation_date date,
	tool_output TEXT,
	status VARCHAR(100),
	validation_output TEXT,

	FOREIGN KEY (orig_relpath) REFERENCES newspaperarchive(orig_relpath)
);

CREATE INDEX title_format_index ON characterisation_info(orig_relpath, tool);
