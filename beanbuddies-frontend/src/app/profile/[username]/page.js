// src/app/profile/[username]/page.js
import { getPublicProfileByUsername, getPublicCoursesByUsername } from '../../lib/api'; // <-- NOTUN IMPORT
import Link from 'next/link'; 

// Next.js 16 (Turbopack) er jonno
async function loadData(props) {
  const params = await props.params;
  const { username } = params;
  
  try {
    // Ekhon duita API call hobe
    const profile = await getPublicProfileByUsername(username);
    const courses = await getPublicCoursesByUsername(username); // <-- NOTUN API CALL
    return { profile, courses, error: null, username };
  } catch (err) {
    return { profile: null, courses: [], error: err.message, username };
  }
}

export default async function PublicProfilePage(props) {
  
  const { profile, courses, error, username } = await loadData(props); // <-- courses ekhon ekhane

  if (error) {
    return <div style={{textAlign: 'center', padding: '40px', color: 'red'}}>Error: Could not find user '{username}'</div>;
  }

  if (!profile) {
    return <div style={{textAlign: 'center', padding: '40px'}}>Profile not found.</div>;
  }

  return (
    <div>
      <h1 className="page-title">{profile.username}'s Profile</h1>

      <div className="card profile-details">
        <h2>Instructor Details</h2>
        <div>
          <label>Full Name</label>
          <p>{profile.username || 'Not set'}</p>
        </div>
        <div>
          <label>Role</label>
          <p>{profile.role}</p>
        </div>
        <div>
          <label>Email</label>
          <p>{profile.email}</p>
        </div>
      </div>
      
      {/* --- EI SECTION-TA UPDATE KORA HOYECHE --- */}
      <div className="card">
        <h2>Courses by {profile.username}</h2>
        {courses.length > 0 ? (
          <div className="course-grid">
            {courses.map(course => (
              <div key={course.id} className="course-card">
                <Link href={`/course/${course.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                  <div className="course-card-content">
                    <h2 className="course-card-title">{course.title}</h2>
                    <p className="course-card-desc">{course.description}</p>
                  </div>
                </Link>
                <div className="course-card-footer" style={{ padding: '0 1.5rem 1.5rem 1.5rem' }}>
                  <span className="course-card-price">${course.price}</span>
                  <span className="course-card-instructor">By {course.instructorName}</span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p>{profile.username} has not created any courses yet.</p>
        )}
      </div>
    </div>
  );
}