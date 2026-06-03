INSERT IGNORE INTO patients (id, first_name, last_name, age, mobile, email, created_at, updated_at)
VALUES
  (1, 'John',  'Doe',    35, '9876543210', 'john.doe@example.com',    NOW(), NOW()),
  (2, 'Jane',  'Smith',  28, '9123456780', 'jane.smith@example.com',  NOW(), NOW()),
  (3, 'Alice', 'Johnson',42, '9988776655', 'alice.j@example.com',     NOW(), NOW());
