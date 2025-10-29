-- ===============================
--  IAM Database Initialization
--  Compatible with MySQL 8+
-- ===============================

-- Insert default roles
INSERT IGNORE INTO roles (name, description) VALUES
('ADMIN', 'Administrator with full access'),
('USER', 'Standard user with limited access'),
('MANAGER', 'Manager with elevated privileges');

-- Insert default permissions
INSERT IGNORE INTO permissions (name, description, resource_type) VALUES
('READ_USERS', 'Can view user information', 'USER'),
('WRITE_USERS', 'Can create and update users', 'USER'),
('DELETE_USERS', 'Can delete users', 'USER'),
('READ_SCREENS', 'Can view screen configurations', 'SCREEN'),
('WRITE_SCREENS', 'Can configure screen access', 'SCREEN'),
('READ_AUDIT_LOGS', 'Can view audit logs', 'AUDIT'),
('MANAGE_ROLES', 'Can manage roles and permissions', 'ROLE');

-- Assign permissions to ADMIN
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- Assign limited permissions to USER
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'USER' AND p.name IN ('READ_USERS', 'READ_SCREENS');

-- Insert default screens
INSERT IGNORE INTO screens (screen_id, name, description, path, is_active) VALUES
('SCR_LOGIN_001', 'Login Screen', 'User authentication page', '/login', true),
('SCR_REGISTER_001', 'Registration Screen', 'New user registration', '/register', true),
('SCR_DASHBOARD_001', 'Dashboard', 'Main user dashboard', '/dashboard', true),
('SCR_PROFILE_001', 'Profile Settings', 'User profile management', '/profile', true),
('SCR_MFA_SETUP_001', 'MFA Setup', 'Multi-factor authentication setup', '/mfa-setup', true),
('SCR_ADMIN_DASH_001', 'Admin Dashboard', 'Administrative control panel', '/admin', true),
('SCR_USER_MGMT_001', 'User Management', 'Manage system users', '/admin/users', true),
('SCR_SETTINGS_001', 'System Settings', 'System configuration', '/settings', true);

-- Assign USER role screens
INSERT IGNORE INTO screen_roles (screen_id, role_id)
SELECT s.id, r.id FROM screens s, roles r
WHERE r.name = 'USER' AND s.screen_id IN (
  'SCR_DASHBOARD_001', 'SCR_PROFILE_001', 'SCR_MFA_SETUP_001'
);

-- Assign ADMIN all screens
INSERT IGNORE INTO screen_roles (screen_id, role_id)
SELECT s.id, r.id FROM screens s, roles r
WHERE r.name = 'ADMIN';

-- Assign MANAGER all screens except user management
INSERT IGNORE INTO screen_roles (screen_id, role_id)
SELECT s.id, r.id FROM screens s, roles r
WHERE r.name = 'MANAGER' AND s.screen_id NOT IN ('SCR_USER_MGMT_001');

-- Insert error codes
INSERT IGNORE INTO error_codes (error_code, message, description, http_status, screen_id) VALUES
('ERR_AUTH_001', 'Invalid credentials', 'Username or password is incorrect', 401, 'SCR_LOGIN_001'),
('ERR_AUTH_002', 'Account locked', 'Account has been locked due to multiple failed login attempts', 403, 'SCR_LOGIN_001'),
('ERR_AUTH_003', 'Too many attempts', 'Too many failed login attempts', 429, 'SCR_LOGIN_001'),
('ERR_MFA_001', 'MFA not enabled', 'Multi-factor authentication is not enabled for this user', 400, 'SCR_MFA_SETUP_001'),
('ERR_MFA_002', 'Invalid MFA code', 'The provided MFA code is invalid or expired', 401, 'SCR_LOGIN_001'),
('ERR_USER_001', 'User not found', 'The specified user does not exist', 404, NULL),
('ERR_SCREEN_001', 'Screen not found', 'The requested screen does not exist', 404, NULL),
('ERR_ACCESS_001', 'Access denied', 'You do not have permission to access this resource', 403, NULL),
('ERR_VAL_001', 'Validation error', 'Input validation failed', 400, NULL),
('ERR_REG_001', 'Username exists', 'The username is already taken', 409, 'SCR_REGISTER_001'),
('ERR_REG_002', 'Email exists', 'The email address is already registered', 409, 'SCR_REGISTER_001'),
('ERR_SYS_001', 'System error', 'An unexpected system error occurred', 500, NULL);

-- Create default admin user (password: Admin@123)
INSERT IGNORE INTO users (username, email, password, account_enabled, account_locked, mfa_enabled, created_at, updated_at)
VALUES (
  'admin',
  'admin@example.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMye1J9XT7KQUYLy4f7vPvVj7fLY5hq5xIC',
  true, false, false,
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Assign ADMIN role to admin user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';

-- Create test user (password: User@123)
INSERT IGNORE INTO users (username, email, password, account_enabled, account_locked, mfa_enabled, created_at, updated_at)
VALUES (
  'testuser',
  'testuser@example.com',
  '$2a$10$rZvbM6qQ9YlHJgXK5J5kY.6hBx7nK6TqLWpQpE8LKxZmX9YmqxN2e',
  true, false, false,
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Assign USER role to test user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'testuser' AND r.name = 'USER';

-- Add indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_audit_logs_username ON audit_logs(username);
CREATE INDEX idx_audit_logs_screen_id ON audit_logs(screen_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_screens_screen_id ON screens(screen_id);
CREATE INDEX idx_error_codes_error_code ON error_codes(error_code);

-- Summary query (optional)
SELECT 'Database initialization completed!' AS status;
