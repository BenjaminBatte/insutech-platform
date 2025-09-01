

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8181';
const USER_API_BASE_URL = `${API_BASE_URL}/api/users`;

const handleResponse = async (response) => {
  if (!response.ok) {
    const errorMessage = await response.text();
    throw new Error(`Error ${response.status}: ${errorMessage}`);
  }
  return response.json();
};

// ✅ Get all users
export const getAllUsers = async () => {
  try {
    const response = await fetch(USER_API_BASE_URL);
    return await handleResponse(response);
  } catch (error) {
    console.error("Error fetching users:", error);
    throw error;
  }
};

// ✅ Get user by ID
export const getUserById = async (id) => {
  try {
    const response = await fetch(`${USER_API_BASE_URL}/${id}`);
    return await handleResponse(response);
  } catch (error) {
    console.error("Error fetching user:", error);
    throw error;
  }
};

// ✅ Get users by role
export const getUsersByRole = async (role) => {
  try {
    const response = await fetch(`${USER_API_BASE_URL}/role/${role}`);
    return await handleResponse(response);
  } catch (error) {
    console.error("Error fetching users by role:", error);
    throw error;
  }
};

// ✅ Create user
export const createUser = async (userData) => {
  try {
    const response = await fetch(USER_API_BASE_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });
    return await handleResponse(response);
  } catch (error) {
    console.error("Error creating user:", error);
    throw error;
  }
};

// ✅ Update user
export const updateUser = async (id, userData) => {
  try {
    const response = await fetch(`${USER_API_BASE_URL}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });
    return await handleResponse(response);
  } catch (error) {
    console.error("Error updating user:", error);
    throw error;
  }
};

// ✅ Delete user
export const deleteUser = async (id) => {
  try {
    const response = await fetch(`${USER_API_BASE_URL}/${id}`, {
      method: "DELETE",
    });
    if (!response.ok) throw new Error(`Error deleting user: ${response.status}`);
    return true;
  } catch (error) {
    console.error("Error deleting user:", error);
    throw error;
  }
};

// ✅ Local client-side search (filters already fetched users)
export const searchUsers = async (searchTerm) => {
  try {
    const users = await getAllUsers();
    return users.filter(
      (user) =>
        user.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.role.toLowerCase().includes(searchTerm.toLowerCase())
    );
  } catch (error) {
    console.error("Error searching users:", error);
    throw error;
  }
};
