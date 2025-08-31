-- Fix existing null rating_count values in driver and rider tables
-- Run this SQL script in your PostgreSQL database

-- Update driver table - set rating_count to 0 where it's null
UPDATE driver SET rating_count = 0 WHERE rating_count IS NULL;

-- Update rider table - set rating_count to 0 where it's null  
UPDATE rider SET rating_count = 0 WHERE rating_count IS NULL;

-- Verify the updates
SELECT 'driver' as table_name, COUNT(*) as total_records, 
       COUNT(rating_count) as non_null_rating_count 
FROM driver
UNION ALL
SELECT 'rider' as table_name, COUNT(*) as total_records, 
       COUNT(rating_count) as non_null_rating_count 
FROM rider;
