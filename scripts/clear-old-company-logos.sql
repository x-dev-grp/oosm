UPDATE company_profile
SET logo_data = NULL,
    logo_content_type = NULL
WHERE LOWER(legal_name) LIKE '%abiooc%'
   OR id = '66c5ca78-35c8-4083-a87f-9ffb396a1762';
