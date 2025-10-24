# Admin Authentication Setup

## Overview
The Home4Paws application now includes admin authentication functionality. Admins can access the admin panel using dedicated credentials.

## Admin Credentials
- **Username**: `admin`
- **Password**: `Admin123!`
- **Email**: `admin@home4paws.com`
- **Name**: System Administrator

## API Endpoints

### Admin Login
- **Endpoint**: `POST /api/admin/login`
- **Description**: Authenticate admin user and receive JWT token
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "Admin123!"
  }
  ```
- **Response**:
  ```json
  {
    "token": "jwt_token_here",
    "username": "admin",
    "roles": ["ROLE_ADMIN"],
    "message": "Admin login successful"
  }
  ```

### Admin Profile
- **Endpoint**: `GET /api/admin/profile`
- **Description**: Get current admin user information
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**:
  ```json
  {
    "id": 1,
    "username": "admin",
    "email": "admin@home4paws.com",
    "firstName": "System",
    "lastName": "Administrator",
    "enabled": true
  }
  ```

### Check Admin Status
- **Endpoint**: `GET /api/admin/check`
- **Description**: Check if current user is an admin
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**:
  ```json
  {
    "isAdmin": true,
    "username": "admin"
  }
  ```

## Security Configuration
- Admin login endpoint (`/api/admin/login`) is publicly accessible
- All other admin endpoints (`/api/admin/**`) require `ROLE_ADMIN`
- JWT tokens are used for authentication
- CORS is enabled for cross-origin requests

## Frontend Integration
To integrate with your admin frontend:

1. **Login**: Send POST request to `/api/admin/login` with admin credentials
2. **Store Token**: Save the JWT token from the response
3. **Authenticate Requests**: Include `Authorization: Bearer <token>` header in all admin API calls
4. **Redirect**: After successful login, redirect to admin panel

## Example Frontend Login Code
```javascript
const adminLogin = async (username, password) => {
  try {
    const response = await fetch('/api/admin/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password })
    });
    
    const data = await response.json();
    
    if (response.ok) {
      // Store token and redirect to admin panel
      localStorage.setItem('adminToken', data.token);
      window.location.href = '/admin-panel';
    } else {
      console.error('Login failed:', data.error);
    }
  } catch (error) {
    console.error('Login error:', error);
  }
};
```

## Database
The admin user is automatically created when the application starts if it doesn't exist. The admin has the `ROLE_ADMIN` role which grants access to admin-only endpoints.
