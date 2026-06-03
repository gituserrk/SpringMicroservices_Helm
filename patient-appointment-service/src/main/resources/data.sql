INSERT INTO appointments (id, patient_id, doctor_name, appointment_date, status, created_at, updated_at)
VALUES
  (1, 1, 'Dr. Smith',   '2026-07-10', 'BOOKED',    NOW(), NOW()),
  (2, 2, 'Dr. Patel',   '2026-07-12', 'BOOKED',    NOW(), NOW()),
  (3, 1, 'Dr. Williams','2026-07-15', 'CANCELLED',  NOW(), NOW());
