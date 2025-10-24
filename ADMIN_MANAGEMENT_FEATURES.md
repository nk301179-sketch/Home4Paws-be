# Admin Management Features

## Overview
The Home4Paws application now includes comprehensive admin management features that allow administrators to view and delete user accounts, reports, and surrender submissions.

## Admin Authentication
- **Username**: `admin`
- **Password**: `Admin123!`
- **Login Endpoint**: `POST /api/admin/login`

## New Admin Endpoints

### User Management

#### Get All Users
- **Endpoint**: `GET /api/admin/users`
- **Description**: Retrieve list of all users in the system
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: Array of user objects with id, username, email, firstName, lastName, enabled status, and roles

#### Delete User
- **Endpoint**: `DELETE /api/admin/users/{userId}`
- **Description**: Delete a user account by ID
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: 
  ```json
  {
    "message": "User deleted successfully",
    "userId": 123
  }
  ```

### Report Management

#### Get All Reports
- **Endpoint**: `GET /api/admin/reports`
- **Description**: Retrieve list of all reports in the system
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: Array of report objects with id, name, phone, description, location, photos, userId, etc.

#### Delete Report
- **Endpoint**: `DELETE /api/admin/reports/{reportId}`
- **Description**: Delete a report by ID
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: 
  ```json
  {
    "message": "Report deleted successfully",
    "reportId": 456
  }
  ```

### Surrender Submission Management

#### Get All Surrender Submissions
- **Endpoint**: `GET /api/admin/surrender-submissions`
- **Description**: Retrieve list of all surrender submissions in the system
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: Array of surrender submission objects with all details including owner info, dog info, photos, status, etc.

#### Delete Surrender Submission
- **Endpoint**: `DELETE /api/admin/surrender-submissions/{submissionId}`
- **Description**: Delete a surrender submission by ID
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: 
  ```json
  {
    "message": "Surrender submission deleted successfully",
    "submissionId": 789
  }
  ```

## Security Features

### Authentication & Authorization
- All admin management endpoints require `ROLE_ADMIN`
- JWT token authentication is mandatory
- Admin status is verified on each request
- Proper error handling for unauthorized access

### Error Responses
- **401 Unauthorized**: When user is not authenticated or not an admin
- **404 Not Found**: When the requested resource doesn't exist
- **500 Internal Server Error**: When deletion fails due to server errors

## Example Usage

### 1. Admin Login
```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "Admin123!"}'
```

### 2. Get All Users
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <jwt_token>"
```

### 3. Delete a User
```bash
curl -X DELETE http://localhost:8080/api/admin/users/123 \
  -H "Authorization: Bearer <jwt_token>"
```

### 4. Get All Reports
```bash
curl -X GET http://localhost:8080/api/admin/reports \
  -H "Authorization: Bearer <jwt_token>"
```

### 5. Delete a Report
```bash
curl -X DELETE http://localhost:8080/api/admin/reports/456 \
  -H "Authorization: Bearer <jwt_token>"
```

### 6. Get All Surrender Submissions
```bash
curl -X GET http://localhost:8080/api/admin/surrender-submissions \
  -H "Authorization: Bearer <jwt_token>"
```

### 7. Delete a Surrender Submission
```bash
curl -X DELETE http://localhost:8080/api/admin/surrender-submissions/789 \
  -H "Authorization: Bearer <jwt_token>"
```

## Frontend Integration

### Admin Dashboard Features
1. **User Management Panel**
   - Display all users in a table
   - Show user details (name, email, registration date, status)
   - Delete button for each user
   - Search and filter functionality

2. **Report Management Panel**
   - Display all reports with photos
   - Show report details (reporter, location, description)
   - Delete button for each report
   - Filter by date, location, or status

3. **Surrender Submission Panel**
   - Display all surrender submissions
   - Show dog details, owner information, and photos
   - Delete button for each submission
   - Filter by status, urgency, or date

### Implementation Notes
- All endpoints return proper HTTP status codes
- Error messages are user-friendly
- Photo files are automatically cleaned up when deleting reports or surrender submissions
- Database relationships are properly handled during deletions

## Database Impact
- User deletion removes the user and their associated data
- Report deletion removes the report and associated photo files
- Surrender submission deletion removes the submission and associated photo files
- All operations are transactional and safe

## API Documentation
The endpoints are fully documented with Swagger/OpenAPI annotations, making them available in the API documentation at `/swagger-ui.html` when the application is running.
