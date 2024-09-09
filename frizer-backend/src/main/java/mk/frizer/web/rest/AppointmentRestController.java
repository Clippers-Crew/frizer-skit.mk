package mk.frizer.web.rest;

import mk.frizer.domain.Appointment;
import mk.frizer.domain.dto.simple.AppointmentSimpleDTO;
import mk.frizer.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mk.frizer.domain.dto.*;

import java.util.List;

@RestController
@RequestMapping({ "/api/appointments", "/api/appointment" })
@CrossOrigin(origins = {"localhost:3000","localhost:3001", "localhost:8080"})
public class AppointmentRestController {
    private final AppointmentService appointmentService;

    public AppointmentRestController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping()
    public List<AppointmentSimpleDTO> getAllAppointments() {
        return appointmentService.getAppointments().stream().map(Appointment::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentSimpleDTO> getAppointmentById(@PathVariable Long id) {
        return this.appointmentService.getAppointmentById(id)
                .map(appointment -> ResponseEntity.ok().body(appointment.toDto()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<AppointmentSimpleDTO> createAppointment(@RequestBody AppointmentAddDTO appointmentAddDTO) {
        return this.appointmentService.createAppointment(appointmentAddDTO)
                .map(appointment -> ResponseEntity.ok().body(appointment.toDto()))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<AppointmentSimpleDTO> deleteAppointmentById(@PathVariable Long id) {
        return this.appointmentService.deleteAppointmentById(id)
                .map(appointment -> ResponseEntity.ok().body(appointment.toDto()))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/attended/{id}")
    public ResponseEntity<AppointmentSimpleDTO> changeAttendanceForAppointment(@PathVariable Long id) {
        return this.appointmentService.changeUserAttendanceAppointment(id)
                .map(appointment -> ResponseEntity.ok().body(appointment.toDto()))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

}
