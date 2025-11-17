// app/page.js
import Link from 'next/link';
import { getPublicCourses } from './lib/api';

// This is a Server Component, so we can make it async
export default async function HomePage() {
  let courses = [];
  let error = null;

  try {
    // This fetches data on the server before the page is sent
    courses = await getPublicCourses();
  } catch (err) {
    error = err.message;
  }

  return (
    <div>
      <h1 className="page-title">Explore Courses</h1>
      {error && <p className="error-message">{error}</p>}
      
      <div className="course-grid">
        {courses.map((course) => (
          // Ekhane course-card-take div banano hocche jate bhetore duita alada link thakte pare
          <div key={course.id} className="course-card">
            <Link href={`/course/${course.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div className="course-card-content">
                <h2 className="course-card-title">{course.title}</h2>
                <p className="course-card-desc">{course.description}</p>
              </div>
            </Link>
            {/* Footer-take alada kora hocche */}
            <div className="course-card-footer" style={{ padding: '0 1.5rem 1.5rem 1.5rem' }}>
              <span className="course-card-price">${course.price}</span>
              
              {/* --- EIKHANE LINK ADD KORA HOYECHE --- */}
              <Link 
                href={`/profile/${course.instructorName}`} 
                className="course-card-instructor"
                style={{ zIndex: 10, position: 'relative' }} // Link-take clickable korar jonno
              >
                By {course.instructorName}
              </Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}