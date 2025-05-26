# Performance Monitoring Configuration
# This file documents the key performance metrics to monitor for the optimized delete operations

## Database Performance Metrics

### Query Performance
- **Delete Operation Latency**: Target < 50ms (99th percentile)
- **Batch Delete Latency**: Target < 200ms for up to 100 records
- **Index Hit Ratio**: Target > 95% for tutor_application table

### Database Connection Pool
- **Active Connections**: Monitor peak usage
- **Connection Wait Time**: Target < 10ms
- **Connection Pool Efficiency**: Target > 99%

## Application Performance Metrics

### API Response Times
- **DELETE /api/tutor-applications/{studentId}**: Target < 150ms
- **Async Delete Operations**: Target < 100ms for completion notification

### Error Rates
- **Database Constraint Violations**: Target < 0.1%
- **Transaction Rollback Rate**: Target < 0.01%
- **Application Exception Rate**: Target < 0.05%

## Monitoring Queries

### Index Usage Analysis
```sql
-- Check index usage for tutor_application table
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes 
WHERE tablename = 'tutor_application'
ORDER BY idx_scan DESC;
```

### Query Performance Analysis
```sql
-- Monitor slow queries related to tutor applications
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows
FROM pg_stat_statements 
WHERE query LIKE '%tutor_application%'
ORDER BY total_time DESC
LIMIT 10;
```

### Lock Monitoring
```sql
-- Monitor lock contention on tutor_application table
SELECT 
    locktype,
    relation::regclass,
    mode,
    granted,
    pid,
    query
FROM pg_locks l
JOIN pg_stat_activity a ON l.pid = a.pid
WHERE relation::regclass::text = 'tutor_application';
```

## Alerting Thresholds

### Critical Alerts
- Delete operation latency > 200ms (99th percentile)
- Database connection pool exhaustion (> 90% utilization)
- Transaction rollback rate > 1%

### Warning Alerts
- Delete operation latency > 100ms (95th percentile)
- Index hit ratio < 95%
- Connection wait time > 50ms

## Performance Optimization Checklist

### Completed ✅
- [x] Implemented single-query delete operations
- [x] Added composite indexes for optimal query performance
- [x] Enhanced error handling and logging
- [x] Implemented asynchronous processing
- [x] Added comprehensive input validation

### Pending 🔄
- [ ] Implement Redis caching for frequently accessed data
- [ ] Add database connection pooling metrics
- [ ] Implement circuit breaker pattern for resilience
- [ ] Add comprehensive integration tests
- [ ] Implement performance regression testing

## Load Testing Scenarios

### Scenario 1: High Delete Volume
- **Test**: 1000 concurrent delete operations
- **Expected**: < 150ms average response time
- **Success Criteria**: No database lock contention

### Scenario 2: Mixed Workload
- **Test**: 50% reads, 30% writes, 20% deletes
- **Expected**: Consistent performance across all operations
- **Success Criteria**: No performance degradation

### Scenario 3: Stress Test
- **Test**: 5000 operations/minute sustained load
- **Expected**: System remains stable
- **Success Criteria**: No memory leaks or connection pool exhaustion
