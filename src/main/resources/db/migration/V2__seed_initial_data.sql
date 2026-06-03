INSERT INTO authentication_methods (name)
VALUES
    ('NONE'),
    ('API_KEY'),
    ('BEARER'),
    ('OAUTH2'),
    ('BASIC_AUTH'),
    ('HMAC')
ON CONFLICT DO NOTHING;
