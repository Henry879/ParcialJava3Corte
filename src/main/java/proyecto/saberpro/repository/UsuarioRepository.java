package proyecto.saberpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto.saberpro.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByDocumento(String documento);  // <-- IMPORTANTE
}
