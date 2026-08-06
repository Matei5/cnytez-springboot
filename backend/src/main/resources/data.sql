INSERT INTO filters (id, name, label) VALUES (0, 'none', 'No filter')
ON CONFLICT (id)
DO UPDATE SET
    name = EXCLUDED.name,
    label = EXCLUDED.label;

INSERT INTO filters (id, name, label) VALUES (1, 'grayscale', 'Grayscale')
ON CONFLICT (id)
DO UPDATE SET
    name = EXCLUDED.name,
    label = EXCLUDED.label;

INSERT INTO filters (id, name, label) VALUES (2, 'sepia', 'Sepia')
ON CONFLICT (id)
DO UPDATE SET
    name = EXCLUDED.name,
    label = EXCLUDED.label;

INSERT INTO filters (id, name, label) VALUES (3, 'inverted', 'Inverted')
ON CONFLICT (id)
DO UPDATE SET
    name = EXCLUDED.name,
    label = EXCLUDED.label;

INSERT INTO filters (id, name, label) VALUES (4, 'blur', 'Blur')
ON CONFLICT (id)
DO UPDATE SET
    name = EXCLUDED.name,
    label = EXCLUDED.label;

INSERT INTO filters (id, name, label) VALUES (5, 'pixelated', 'Pixelated')
ON CONFLICT (id)
DO UPDATE SET
    name = EXCLUDED.name,
    label = EXCLUDED.label;
