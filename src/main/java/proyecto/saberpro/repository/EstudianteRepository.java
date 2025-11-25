package proyecto.saberpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto.saberpro.model.Estudiante;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);
}
