-- ---------------------------------------------------------------------------
-- Compatibility fixes for the original PHP "gymnsb" schema.
-- Every statement is idempotent: it is safe to run on every startup.
--
-- Why: the PHP code relied on MySQL's old non-strict mode (inserting rows
-- without values for NOT NULL columns, storing '0000-00-00' dates, putting
-- 10-digit phone numbers in an INT column). Modern MySQL/MariaDB run in
-- strict mode, so these columns are relaxed here.
-- ---------------------------------------------------------------------------
SET SESSION sql_mode = '';

-- Members: optional dates / progress fields get sensible defaults
ALTER TABLE members MODIFY paid_date DATE NULL;
ALTER TABLE members MODIFY progress_date DATE NULL;
ALTER TABLE members MODIFY p_year INT(11) NOT NULL DEFAULT 0;
ALTER TABLE members MODIFY attendance_count INT(100) NOT NULL DEFAULT 0;
ALTER TABLE members MODIFY ini_bodytype VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE members MODIFY curr_bodytype VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE members MODIFY contact VARCHAR(15) NOT NULL;
UPDATE members SET progress_date = NULL WHERE CAST(progress_date AS CHAR) = '0000-00-00';
UPDATE members SET paid_date = NULL WHERE CAST(paid_date AS CHAR) = '0000-00-00';

-- Passwords: room for BCrypt hashes (old MD5 hashes keep working)
ALTER TABLE admin MODIFY password VARCHAR(100) NOT NULL;
ALTER TABLE staffs MODIFY password VARCHAR(100) NOT NULL;
ALTER TABLE members MODIFY password VARCHAR(100) NOT NULL;

-- Staff contact was INT(10): numbers above 2147483647 were truncated
ALTER TABLE staffs MODIFY contact VARCHAR(15) NOT NULL;
ALTER TABLE equipment MODIFY contact VARCHAR(15) NOT NULL;

-- License keys entered in "License Management" (the newest row is the active license)
CREATE TABLE IF NOT EXISTS licenses (
    id           INT(11)      NOT NULL AUTO_INCREMENT,
    license_key  TEXT         NOT NULL,
    customer     VARCHAR(100) NULL,
    expires_on   DATE         NOT NULL,
    activated_at DATETIME     NOT NULL,
    PRIMARY KEY (id)
);
