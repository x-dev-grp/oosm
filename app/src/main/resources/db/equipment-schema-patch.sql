-- Mill equipment registry and external service missions

CREATE TABLE IF NOT EXISTS mill_equipment (
    id UUID PRIMARY KEY,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    tenant_id UUID,
    code VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    equipment_type VARCHAR(50) NOT NULL,
    registration_number VARCHAR(100),
    default_hourly_rate DOUBLE PRECISION DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    hours_operated DOUBLE PRECISION DEFAULT 0,
    last_maintenance_date TIMESTAMP,
    next_maintenance_date TIMESTAMP,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS equipment_service_mission (
    id UUID PRIMARY KEY,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    tenant_id UUID,
    equipment_id UUID NOT NULL REFERENCES mill_equipment(id),
    client_name VARCHAR(255) NOT NULL,
    client_phone VARCHAR(100),
    work_location VARCHAR(500),
    description TEXT,
    operator_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    scheduled_start TIMESTAMP,
    scheduled_end TIMESTAMP,
    completed_at TIMESTAMP,
    billable_hours DOUBLE PRECISION DEFAULT 0,
    hourly_rate DOUBLE PRECISION DEFAULT 0,
    total_amount DOUBLE PRECISION DEFAULT 0,
    payment_method VARCHAR(50),
    paid_amount DOUBLE PRECISION DEFAULT 0,
    unpaid_amount DOUBLE PRECISION DEFAULT 0,
    invoice_reference VARCHAR(255),
    notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_equipment_service_mission_equipment ON equipment_service_mission(equipment_id);
CREATE INDEX IF NOT EXISTS idx_equipment_service_mission_status ON equipment_service_mission(status);
