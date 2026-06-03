package org.modmed.appointmentservice;

import org.junit.jupiter.api.Test;
import org.modmed.appointmentservice.feign.PatientClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class AppointmentServiceApplicationTests {

    // Prevent Feign from making real HTTP calls during context load
    @MockitoBean
    private PatientClient patientClient;

    @Test
    void contextLoads() {
    }
}
