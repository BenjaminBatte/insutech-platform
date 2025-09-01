const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8181';
const MANAGED_POLICY_BASE_URL = `${API_BASE_URL}/api/managed-policies`;


const handleResponse = async (response) => {
  if (!response.ok) {
    const errorMessage = await response.text();
    throw new Error(`Error ${response.status}: ${errorMessage}`);
  }
  return response.json();
};

// ✅ Get all managed policies
export const getAllManagedPolicies = async () => {
  const response = await fetch(MANAGED_POLICY_BASE_URL);
  return handleResponse(response);
};

// ✅ Get managed policies filtered by role/username
export const getManagedPolicies = async (role, username) => {
  const url = username
    ? `${MANAGED_POLICY_BASE_URL}?role=${role}&username=${username}`
    : `${MANAGED_POLICY_BASE_URL}?role=${role}`;
  const response = await fetch(url);
  return handleResponse(response);
};

// ✅ Get a single managed policy by ID
export const getManagedPolicyById = async (id) => {
  const response = await fetch(`${MANAGED_POLICY_BASE_URL}/${id}`);
  return handleResponse(response);
};

// ✅ Create a new managed policy
export const createManagedPolicy = async (policy) => {
  const response = await fetch(MANAGED_POLICY_BASE_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(policy),
  });
  return handleResponse(response);
};

// ✅ Update an existing managed policy
export const updateManagedPolicy = async (id, policyData) => {
  const response = await fetch(`${MANAGED_POLICY_BASE_URL}/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(policyData),
  });
  return handleResponse(response);
};

// ✅ Delete a managed policy by ID
export const deleteManagedPolicy = async (id) => {
  const response = await fetch(`${MANAGED_POLICY_BASE_URL}/${id}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    throw new Error(`Failed to delete managed policy with id ${id}`);
  }
  return true;
};
