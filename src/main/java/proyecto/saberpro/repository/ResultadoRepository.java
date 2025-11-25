package proyecto.saberpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto.saberpro.model.Resultado;
import java.util.List;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {
	long countByEstudianteId(Long estudianteId);
	List<Resultado> findByEstudianteId(Long estudianteId);
}
