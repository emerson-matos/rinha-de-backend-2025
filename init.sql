-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    correlation_id UUID UNIQUE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    processor VARCHAR(20) NOT NULL CHECK (processor IN ('default', 'fallback')),
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster queries on payments-summary endpoint
CREATE INDEX IF NOT EXISTS idx_payments_requested_at ON payments(requested_at);
CREATE INDEX IF NOT EXISTS idx_payments_processor ON payments(processor);
CREATE INDEX IF NOT EXISTS idx_payments_processor_requested_at ON payments(processor, requested_at);

-- Optimize for read-heavy workload
ANALYZE payments;
