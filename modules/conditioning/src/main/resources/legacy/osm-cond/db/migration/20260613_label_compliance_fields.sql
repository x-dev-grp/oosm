-- Tunisia olive oil label compliance fields
ALTER TABLE label_content ADD COLUMN IF NOT EXISTS ingredient_declaration VARCHAR(500);
ALTER TABLE label_content ADD COLUMN IF NOT EXISTS nutrition_declaration_json TEXT;
ALTER TABLE label_content ADD COLUMN IF NOT EXISTS ean13 VARCHAR(13);
ALTER TABLE label_content ADD COLUMN IF NOT EXISTS harvest_year VARCHAR(10);
ALTER TABLE label_content ADD COLUMN IF NOT EXISTS acidity_level VARCHAR(20);
ALTER TABLE label_content ADD COLUMN IF NOT EXISTS brand_name VARCHAR(255);
