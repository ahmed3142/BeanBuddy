// app/lib/api.js

// The base URL for your Spring Boot API
// const API_URL = 'http://localhost:8081/api/v1';

const API_URL = 'https://georgiann-unbribing-elderly.ngrok-free.dev/api/v1';

async function fetchProtected(url, token, options = {}) {
  // ... (baki code same)
  const response = await fetch(`${API_URL}${url}`, {
    ...options,
    headers: {
      ...options.headers,
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`, 
    },
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({})); 
    const message = errorData.message || errorData.detail || response.statusText;
    throw new Error(message); 
  }
  if (response.status === 204) return null;
  return response.json();
}

// --- Public API Functions (No token needed) ---
export const getPublicCourses = async () => {
  const res = await fetch(`${API_URL}/courses/public/all`);
  if (!res.ok) throw new Error('Failed to fetch courses');
  return res.json();
};
export const getPublicProfileByUsername = async (username) => {
  const res = await fetch(`${API_URL}/users/public/${username}`);
  if (!res.ok) throw new Error('Failed to fetch profile');
  return res.json();
};
export const getPublicCoursesByUsername = async (username) => {
  const res = await fetch(`${API_URL}/courses/public/by/{username}`);
  if (!res.ok) throw new Error('Failed to fetch courses');
  return res.json();
};

// --- Protected API Functions (Token required) ---

// --- EI FUNCTION-TA UPDATE KORA HOYECHE ---
// Ekhon eita protected ebong token pathay
export const getCourseDetails = (token, id) => {
  return fetchProtected(`/courses/${id}`, token);
};

export const getMyDashboard = (token) => {
  return fetchProtected('/users/me/dashboard', token);
};
export const getMyProfile = (token) => {
    return fetchProtected('/courses/my-profile', token);
};
export const updateMyProfile = (token, updateData) => {
  return fetchProtected('/users/me', token, {
    method: 'PUT',
    body: JSON.stringify(updateData),
  });
};
export const enrollInCourse = (courseId, token) => {
  return fetchProtected(`/enrollments/enroll/${courseId}`, token, {
    method: 'POST',
  });
};
export const createCourse = (token, courseData) => {
  return fetchProtected('/courses/create', token, {
    method: 'POST',
    body: JSON.stringify(courseData), 
  });
};
export const addLessonToCourse = (token, courseId, lessonData) => {
  return fetchProtected(`/lessons/course/${courseId}`, token, {
    method: 'POST',
    body: JSON.stringify(lessonData), 
  });
};
export const deleteLesson = (token, lessonId) => {
  return fetchProtected(`/lessons/${lessonId}`, token, {
    method: 'DELETE',
  });
};
export const getLessonDetails = (token, lessonId) => {
  return fetchProtected(`/lessons/${lessonId}`, token);
};
export const getCourseComments = (token, courseId) => {
  return fetchProtected(`/courses/${courseId}/comments`, token);
};
export const postCourseComment = (token, courseId, commentData) => {
  return fetchProtected(`/courses/${courseId}/comments`, token, {
    method: 'POST',
    body: JSON.stringify(commentData),
  });
};
export const getLessonComments = (token, lessonId) => {
  return fetchProtected(`/lessons/${lessonId}/comments`, token);
};
export const postLessonComment = (token, lessonId, commentData) => {
  return fetchProtected(`/lessons/${lessonId}/comments`, token, {
    method: 'POST',
    body: JSON.stringify(commentData),
  });
};
export const deleteMyAccount = (token) => {
  return fetchProtected('/users/me', token, {
    method: 'DELETE',
  });
};
export const deleteCourseAsAdmin = (token, courseId) => {
  return fetchProtected(`/admin/courses/${courseId}`, token, {
    method: 'DELETE',
  });
};
export const checkEnrollmentStatus = (token, courseId) => {
  return fetchProtected(`/enrollments/is-enrolled/${courseId}`, token);
};
export const markLessonComplete = (token, lessonId) => {
  return fetchProtected(`/lessons/${lessonId}/complete`, token, {
    method: 'POST',
  });
};

// --- EI NOTUN FUNCTION-TA ADD KORA HOYECHE ---
export const initiatePayment = (token, courseId) => {
  return fetchProtected(`/payment/initiate/${courseId}`, token, {
    method: 'POST',
  });
};