# Admin API Testing Guide

## Prerequisites
1. Start the application: `mvn spring-boot:run`
2. Ensure admin user exists (created by DataInitializer)

## Test Sequence

### 1. Admin Login Test
```bash
# Test admin login
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "Admin123!"}'

# Expected Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "username": "admin",
#   "roles": ["ROLE_ADMIN"],
#   "message": "Admin login successful"
# }
```

### 2. User Management Tests

#### Get All Users
```bash
# Replace <jwt_token> with the token from login response
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <jwt_token>"

# Expected: Array of user objects
```

#### Delete User (if you have test users)
```bash
# Replace <user_id> with actual user ID
curl -X DELETE http://localhost:8080/api/admin/users/<user_id> \
  -H "Authorization: Bearer <jwt_token>"

# Expected Response:
# {
#   "message": "User deleted successfully",
#   "userId": <user_id>
# }
```

### 3. Report Management Tests

#### Get All Reports
```bash
curl -X GET http://localhost:8080/api/admin/reports \
  -H "Authorization: Bearer <jwt_token>"

# Expected: Array of report objects
```

#### Delete Report (if you have test reports)
```bash
# Replace <report_id> with actual report ID
curl -X DELETE http://localhost:8080/api/admin/reports/<report_id> \
  -H "Authorization: Bearer <jwt_token>"

# Expected Response:
# {
#   "message": "Report deleted successfully",
#   "reportId": <report_id>
# }
```

### 4. Surrender Submission Management Tests

#### Get All Surrender Submissions
```bash
curl -X GET http://localhost:8080/api/admin/surrender-submissions \
  -H "Authorization: Bearer <jwt_token>"

# Expected: Array of surrender submission objects
```

#### Delete Surrender Submission (if you have test submissions)
```bash
# Replace <submission_id> with actual submission ID
curl -X DELETE http://localhost:8080/api/admin/surrender-submissions/<submission_id> \
  -H "Authorization: Bearer <jwt_token>"

# Expected Response:
# {
#   "message": "Surrender submission deleted successfully",
#   "submissionId": <submission_id>
# }
```

## Error Testing

### Test Unauthorized Access
```bash
# Try to access admin endpoints without token
curl -X GET http://localhost:8080/api/admin/users

# Expected: 401 Unauthorized
```

### Test Invalid Token
```bash
# Try with invalid token
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer invalid_token"

# Expected: 401 Unauthorized
```

### Test Non-Admin User
```bash
# Login as regular user and try admin endpoints
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "regular_user", "password": "password"}'

# Then try admin endpoint with regular user token
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <regular_user_token>"

# Expected: 401 Unauthorized
```

## Swagger UI Testing
1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Find the "Admin" section
3. Test the endpoints using the Swagger UI interface
4. Use the "Authorize" button to set the JWT token

## Expected Behavior
- All admin endpoints require authentication
- Only users with ROLE_ADMIN can access these endpoints
- Proper error messages are returned for unauthorized access
- Successful operations return appropriate success messages
- Photo files are cleaned up when deleting reports or surrender submissions
