package com.beanbuddies.BeanBuddies;

import com.beanbuddies.BeanBuddies.model.Course;
import com.beanbuddies.BeanBuddies.model.PaymentTransaction;
import com.beanbuddies.BeanBuddies.model.Role;
import com.beanbuddies.BeanBuddies.model.User;
import com.beanbuddies.BeanBuddies.repository.CourseRepository;
import com.beanbuddies.BeanBuddies.repository.PaymentTransactionRepository;
import com.beanbuddies.BeanBuddies.repository.UserRepository;
import com.beanbuddies.BeanBuddies.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentIpnIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @MockBean
    private EnrollmentService enrollmentService; // prevent actual enrollment side-effects

    @Test
    public void ipn_updates_transaction_to_paid() throws Exception {
        // create a user
        User user = new User();
        user.setSupabaseId("test-supabase");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setRole(Role.ROLE_STUDENT);
        user = userRepository.save(user);

        // create a course
        Course course = new Course();
        course.setTitle("T");
        course.setDescription("D");
        course.setPrice(200.00);
        course = courseRepository.save(course);

        // create a pending payment transaction
        PaymentTransaction tx = new PaymentTransaction();
        tx.setTransactionId("BB-INT-1");
        tx.setSessionKey("sess-1");
        tx.setStudent(user);
        tx.setCourse(course);
        tx.setAmount(200.00);
        tx.setStatus("PENDING");
        transactionRepository.save(tx);

        String form = "tran_id=BB-INT-1&status=VALID&amount=200.00&sessionkey=sess-1";

        mockMvc.perform(post("/api/v1/payment/ipn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(form))
                .andExpect(status().isOk());

        PaymentTransaction updated = transactionRepository.findByTransactionId("BB-INT-1").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("PAID");
    }
}
