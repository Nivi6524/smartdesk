-- =============================================
-- STEP 1: Enable pgvector extension
-- =============================================
CREATE EXTENSION IF NOT EXISTS vector;


-- =============================================
-- STEP 2: USERS TABLE (User Service)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email         VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name     VARCHAR(255) NOT NULL,
  role          VARCHAR(50) DEFAULT 'USER',
  department    VARCHAR(100),
  is_active     BOOLEAN DEFAULT TRUE,
  created_at    TIMESTAMP DEFAULT NOW(),
  updated_at    TIMESTAMP DEFAULT NOW()
);


-- =============================================
-- STEP 3: REFRESH TOKENS TABLE (User Service)
-- =============================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    UUID REFERENCES users(id) ON DELETE CASCADE,
  token      TEXT NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);


-- =============================================
-- STEP 4: TICKETS TABLE (Ticket Service)
-- =============================================
CREATE TABLE IF NOT EXISTS tickets (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title         VARCHAR(255) NOT NULL,
  description   TEXT NOT NULL,
  status        VARCHAR(50)  DEFAULT 'OPEN',
  priority      VARCHAR(50)  DEFAULT 'MEDIUM',
  category      VARCHAR(100),
  created_by    UUID REFERENCES users(id),
  assigned_to   UUID REFERENCES users(id),
  resolution    TEXT,
  ai_suggestion TEXT,
  sla_due_at    TIMESTAMP,
  created_at    TIMESTAMP DEFAULT NOW(),
  updated_at    TIMESTAMP DEFAULT NOW(),
  resolved_at   TIMESTAMP
);


-- =============================================
-- STEP 5: TICKET COMMENTS TABLE (Ticket Service)
-- =============================================
CREATE TABLE IF NOT EXISTS ticket_comments (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ticket_id  UUID REFERENCES tickets(id) ON DELETE CASCADE,
  author_id  UUID REFERENCES users(id),
  content    TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);


-- =============================================
-- STEP 6: TICKET EMBEDDINGS TABLE (AI Service)
-- =============================================
CREATE TABLE IF NOT EXISTS ticket_embeddings (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ticket_id  UUID REFERENCES tickets(id) ON DELETE CASCADE,
  embedding  vector(768),
  content    TEXT NOT NULL,
  metadata   JSONB,
  created_at TIMESTAMP DEFAULT NOW()
);


-- =============================================
-- STEP 7: NOTIFICATIONS TABLE (Notification Service)
-- =============================================
CREATE TABLE IF NOT EXISTS notifications (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    UUID REFERENCES users(id) ON DELETE CASCADE,
  ticket_id  UUID REFERENCES tickets(id) ON DELETE CASCADE,
  type       VARCHAR(50) NOT NULL,
  message    TEXT NOT NULL,
  is_read    BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW()
);


-- =============================================
-- STEP 8: AUDIT LOGS TABLE (Analytics Service)
-- =============================================
CREATE TABLE IF NOT EXISTS audit_logs (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID REFERENCES users(id),
  ticket_id   UUID REFERENCES tickets(id),
  action      VARCHAR(100) NOT NULL,
  old_value   TEXT,
  new_value   TEXT,
  performed_at TIMESTAMP DEFAULT NOW()
);


-- =============================================
-- STEP 9: ALL INDEXES (for fast queries)
-- =============================================

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_email
  ON users(email);

CREATE INDEX IF NOT EXISTS idx_users_role
  ON users(role);

-- Tickets indexes
CREATE INDEX IF NOT EXISTS idx_tickets_status
  ON tickets(status);

CREATE INDEX IF NOT EXISTS idx_tickets_priority
  ON tickets(priority);

CREATE INDEX IF NOT EXISTS idx_tickets_created_by
  ON tickets(created_by);

CREATE INDEX IF NOT EXISTS idx_tickets_assigned_to
  ON tickets(assigned_to);

CREATE INDEX IF NOT EXISTS idx_tickets_created_at
  ON tickets(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_open_tickets
  ON tickets(created_at)
  WHERE status = 'OPEN';

-- Ticket embeddings vector index (for AI similarity search)
CREATE INDEX IF NOT EXISTS idx_ticket_embedding_ivfflat
  ON ticket_embeddings
  USING ivfflat (embedding vector_cosine_ops)
  WITH (lists = 100);

-- Notifications indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user_id
  ON notifications(user_id);

CREATE INDEX IF NOT EXISTS idx_notifications_unread
  ON notifications(user_id)
  WHERE is_read = FALSE;

-- Audit logs indexes
CREATE INDEX IF NOT EXISTS idx_audit_logs_ticket_id
  ON audit_logs(ticket_id);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id
  ON audit_logs(user_id);


-- =============================================
-- STEP 10: SEED SAMPLE DATA (for testing AI)
-- =============================================

-- Insert a test admin user (password is 'admin123' - hashed)
INSERT INTO users (email, password_hash, full_name, role, department)
VALUES (
  'admin@smartdesk.com',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
  'Admin User',
  'ADMIN',
  'IT'
) ON CONFLICT (email) DO NOTHING;

-- Insert a test regular user
INSERT INTO users (email, password_hash, full_name, role, department)
VALUES (
  'nivetha@smartdesk.com',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
  'Nivetha',
  'USER',
  'Engineering'
) ON CONFLICT (email) DO NOTHING;

-- Insert sample resolved tickets (AI needs these to learn from)
INSERT INTO tickets (title, description, status, resolution, created_by)
SELECT
  'VPN not connecting',
  'Cannot connect to company VPN from home office. Getting timeout error.',
  'RESOLVED',
  '1. Restart VPN client completely. 2. Check your firewall - allow VPN app. 3. Re-enter your credentials. 4. Try switching VPN server location.',
  id
FROM users WHERE email = 'admin@smartdesk.com'
ON CONFLICT DO NOTHING;

INSERT INTO tickets (title, description, status, resolution, created_by)
SELECT
  'Cannot access shared drive',
  'Network shared drive not visible after Windows update was installed.',
  'RESOLVED',
  '1. Go to File Explorer > Map Network Drive. 2. Run: ipconfig /flushdns in cmd. 3. Restart your PC. 4. If issue persists, re-join domain.',
  id
FROM users WHERE email = 'admin@smartdesk.com'
ON CONFLICT DO NOTHING;

INSERT INTO tickets (title, description, status, resolution, created_by)
SELECT
  'Outlook not syncing emails',
  'Microsoft Outlook is not showing new emails. Last sync was 2 hours ago.',
  'RESOLVED',
  '1. Check internet connection. 2. File > Account Settings > Test Account. 3. Repair Office via Control Panel. 4. Delete and re-add email account.',
  id
FROM users WHERE email = 'admin@smartdesk.com'
ON CONFLICT DO NOTHING;

INSERT INTO tickets (title, description, status, resolution, created_by)
SELECT
  'Laptop running very slow',
  'My laptop has become extremely slow after recent update. Takes 10 minutes to boot.',
  'RESOLVED',
  '1. Open Task Manager - check what is using CPU/RAM. 2. Disable startup programs. 3. Run Disk Cleanup. 4. Check for malware with Windows Defender.',
  id
FROM users WHERE email = 'admin@smartdesk.com'
ON CONFLICT DO NOTHING;

INSERT INTO tickets (title, description, status, resolution, created_by)
SELECT
  'Cannot install software',
  'Getting permission denied error when trying to install new software on my PC.',
  'RESOLVED',
  '1. Right-click installer > Run as Administrator. 2. Contact IT admin for elevated permissions. 3. Check Group Policy restrictions. 4. Use software center if available.',
  id
FROM users WHERE email = 'admin@smartdesk.com'
ON CONFLICT DO NOTHING;