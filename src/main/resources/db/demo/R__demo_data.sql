-- R__demo_data.sql
-- Fantasy merchant demo seed. Runs as a Flyway repeatable migration.
-- Idempotent: truncates the demo tables and reseeds on every checksum change.

TRUNCATE TABLE cart_item, cart, address, customer, account,
             product_category, product, category
    RESTART IDENTITY CASCADE;

INSERT INTO account (id, email, password_hash, role)
VALUES (1, 'customer@shop-happens.com', '{noop}123', 'CUSTOMER'),
       (2, 'admin@shop-happens.com', '{noop}123', 'ADMIN');

INSERT INTO customer (account_id) VALUES (1);

INSERT INTO address (customer_id, recipient_name, address_line_1, city,
                     postal_code, country_code, is_default_shipping, is_default_billing)
SELECT c.id, 'Bard the Magnificent Debugger', '12 Potion Alley', 'Greymoor', '35037', 'DE', TRUE, TRUE
FROM customer c
WHERE c.account_id = 1;

INSERT INTO category (name, slug)
VALUES ('⚔️ Weapons & Adventuring', 'weapons-adventuring'),
       ('🧙 Magic Items', 'magic-items'),
       ('🐉 Monsters & Problem Solving', 'monsters-problem-solving'),
       ('🧝 Everyday Goods', 'everyday-goods');

INSERT INTO product (sku, name, description, price, stock_quantity, image_url, active)
VALUES ('WEAP-001', '+1 Sword of Clean Code',
        'Lightweight, well-balanced, and suspiciously free of duplicated logic.', 89.99, 25,
        '/images/product-placeholder.svg', TRUE),
       ('WEAP-002', 'Rubber Duck of Debugging',
        'A wise and patient debugging companion. Grants +5 Debugging when consulted before committing questionable code. It has heard things.',
        18.99, 12, '/images/product-placeholder.svg', TRUE),
       ('WEAP-003', 'Keyboard of Infinite Typing',
        'Legendary artifact. Its previous owner wrote 14,000 lines without testing any of them.',
        129.99, 10, '/images/product-placeholder.svg', TRUE),
       ('WEAP-004', 'Dagger of git revert',
        'For when your heroic git push --force has consequences.', 39.99, 20,
        '/images/product-placeholder.svg', TRUE),
       ('WEAP-005', 'Debugger''s Magnifying Glass',
        'Reveals bugs that were "definitely impossible."', 24.99, 30,
        '/images/product-placeholder.svg', TRUE),
       ('WEAP-006', 'Boots of localhost',
        'Extremely fast. Unfortunately, they only work on your machine.', 59.99, 18,
        '/images/product-placeholder.svg', TRUE),
       ('MAGI-001', 'Potion of Unclear Requirements',
        'Effects vary wildly depending on what the client actually meant.', 7.99, 50,
        '/images/product-placeholder.svg', TRUE),
       ('MAGI-002', 'Elixir of Null Safety',
        'Prevents mysterious NullPointerExceptions. Probably.', 12.99, 45,
        '/images/product-placeholder.svg', TRUE),
       ('MAGI-003', 'Mana Potion — Extra Large',
        'Restores 500 mana or one exhausted developer.', 6.49, 60,
        '/images/product-placeholder.svg', TRUE),
       ('MAGI-004', 'Crystal Ball of Observability',
        'Lets you see exactly why production is on fire.', 44.99, 14,
        '/images/product-placeholder.svg', TRUE),
       ('MAGI-005', 'Amulet of CI/CD',
        'Automatically deploys your mistakes to production.', 34.99, 20,
        '/images/product-placeholder.svg', TRUE),
       ('MAGI-006', 'Staff of Dependency Injection',
        'Summons dependencies from mysterious places.', 89.99, 22,
        '/images/product-placeholder.svg', TRUE),
       ('MAGI-007', 'Scroll of Fireball (Level 3)',
        'Some problems solve themselves. This one solves everything else too.', 29.99, 16,
        '/images/product-placeholder.svg', TRUE),
       ('MAGI-008', 'Ring of Infinite Loops',
        'Casts the same spell forever.', 19.99, 0, '/images/product-placeholder.svg', TRUE),
       ('MONS-001', 'Goblin of Technical Debt',
        'Starts small. Somehow lives in production for seven years.', 29.99, 40,
        '/images/product-placeholder.svg', TRUE),
       ('MONS-002', 'Bugbear of Legacy Code',
        'Hasn''t been touched since Java 8 and must not be disturbed.', 39.99, 11,
        '/images/product-placeholder.svg', TRUE),
       ('MONS-003', 'Dragon of Scope Creep',
        'Guards the original requirements and continuously adds new ones.', 199.99, 4,
        '/images/product-placeholder.svg', TRUE),
       ('MONS-004', 'The Final Boss: Production Bug',
        'Cannot be reproduced locally.', 999.99, 1, '/images/product-placeholder.svg', TRUE),
       ('MONS-005', 'Mimic — Definitely Not a Chest',
        'Looks like useful functionality. It is not.', 49.99, 16,
        '/images/product-placeholder.svg', TRUE),
       ('MONS-006', 'Orcish Code Reviewer',
        'Says ''LGTM'' without opening the pull request.', 24.99, 25,
        '/images/product-placeholder.svg', TRUE),
       ('GOOD-001', 'Bag of Holding — Developer Edition',
        'Somehow contains 47 charging cables. We''re not sure how.', 39.99, 23,
        '/images/product-placeholder.svg', TRUE),
       ('GOOD-002', 'Enchanted Coffee Mug',
        'Restores 10 HP per refill.', 14.99, 48, '/images/product-placeholder.svg', TRUE),
       ('GOOD-003', 'Goblin''s USB-C Cable',
        'Compatible with everything. Except the thing you need it for.', 7.99, 100,
        '/images/product-placeholder.svg', TRUE),
       ('GOOD-004', 'Potion of "Works on My Machine"',
        'Temporarily makes all bugs disappear. Unfortunately, only for you.', 9.99, 21,
        '/images/product-placeholder.svg', TRUE),
       ('GOOD-005', '404: Quest Not Found',
        'The quest existed five minutes ago. Nobody knows where it went.', 12.99, 27,
        '/images/product-placeholder.svg', TRUE),
       ('GOOD-006', 'Potion of Database Migration',
        'Drink only after taking a backup.', 29.99, 13, '/images/product-placeholder.svg', TRUE),
       ('GOOD-007', 'D20 of All Arguments',
        'Settles every dispute with great authority. Usually in someone else''s favor.', 9.99, 64,
        '/images/product-placeholder.svg', TRUE);

INSERT INTO product_category (product_id, category_id)
SELECT p.id, c.id
FROM product p
JOIN category c
  ON CASE
       WHEN p.sku LIKE 'WEAP-%' THEN c.slug = 'weapons-adventuring'
       WHEN p.sku LIKE 'MAGI-%' THEN c.slug = 'magic-items'
       WHEN p.sku LIKE 'MONS-%' THEN c.slug = 'monsters-problem-solving'
       WHEN p.sku LIKE 'GOOD-%' THEN c.slug = 'everyday-goods'
       ELSE FALSE
     END;