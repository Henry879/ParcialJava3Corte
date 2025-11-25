package proyecto.saberpro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import proyecto.saberpro.model.*;
import proyecto.saberpro.repository.*;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EstudianteRepository estudianteRepository;
    
    @Autowired
    private CompetenciaRepository competenciaRepository;
    
    @Autowired
    private ResultadoRepository resultadoRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. CREAR USUARIOS DE PRUEBA
        // Coordinador
        if (usuarioRepository.findByDocumento("123") == null) {
            Usuario coordinador = new Usuario();
            coordinador.setDocumento("123");
            coordinador.setPassword("admin123");
            coordinador.setRol("COORDINADOR");
            usuarioRepository.save(coordinador);
            System.out.println("✅ Usuario coordinador creado: 123/admin123");
        }

        // Estudiante
        Usuario estudianteUser = null;
        if (usuarioRepository.findByDocumento("456") == null) {
            estudianteUser = new Usuario();
            estudianteUser.setDocumento("456");
            estudianteUser.setPassword("estudiante123");
            estudianteUser.setRol("ESTUDIANTE");
            usuarioRepository.save(estudianteUser);
            System.out.println("✅ Usuario estudiante creado: 456/estudiante123");
        } else {
            estudianteUser = usuarioRepository.findByDocumento("456");
        }

        // 2. CREAR ESTUDIANTE ASOCIADO
        if (estudianteRepository.findByNumeroDocumento("456").isEmpty()) {
            Estudiante estudiante = new Estudiante();
            estudiante.setTipoDocumento("CC");
            estudiante.setNumeroDocumento("456");
            estudiante.setNombres("Ana");
            estudiante.setApellidos("García López");
            estudiante.setCorreo("ana.garcia@email.com");
            estudiante.setTelefono("3001234567");
            estudiante.setActivo(true);
            estudiante.setUsuario(estudianteUser);
            estudiante.setResultadosDisponibles(true); // ← TIENE RESULTADOS
            estudianteRepository.save(estudiante);
            System.out.println("✅ Estudiante creado: Ana García");
        }
        

        // 3. CREAR COMPETENCIAS DE PRUEBA
        if (competenciaRepository.count() == 0) {
            Competencia comp1 = new Competencia();
            comp1.setNombre("Razonamiento Cuantitativo");
            comp1.setDescripcion("Habilidad para resolver problemas matemáticos");
            competenciaRepository.save(comp1);

            Competencia comp2 = new Competencia();
            comp2.setNombre("Lectura Crítica");
            comp2.setDescripcion("Comprensión y análisis de textos");
            competenciaRepository.save(comp2);

            Competencia comp3 = new Competencia();
            comp3.setNombre("Inglés");
            comp3.setDescripcion("Comprensión de lectura en inglés");
            competenciaRepository.save(comp3);

            Competencia comp4 = new Competencia();
            comp4.setNombre("Competencias Ciudadanas");
            comp4.setDescripcion("Habilidades para la convivencia ciudadana");
            competenciaRepository.save(comp4);

            Competencia comp5 = new Competencia();
            comp5.setNombre("Comunicación Escrita");
            comp5.setDescripcion("Habilidad para producir textos escritos");
            competenciaRepository.save(comp5);

            System.out.println("✅ Competencias de ejemplo creadas");
        }

        // 4. CREAR RESULTADOS DE PRUEBA CON LOS NUEVOS CAMPOS
        if (resultadoRepository.count() == 0) {
            Estudiante estudiante = estudianteRepository.findByNumeroDocumento("456").get();
            
            // Buscar competencias
            Competencia razonamiento = competenciaRepository.findAll().get(0);
            Competencia lectura = competenciaRepository.findAll().get(1);
            Competencia ingles = competenciaRepository.findAll().get(2);
            Competencia ciudadanas = competenciaRepository.findAll().get(3);
            Competencia escrita = competenciaRepository.findAll().get(4);
            
            // Resultados para SABER_PRO - Estudiante con nivel 4 (máximo beneficio)
            Resultado resultado1 = new Resultado(estudiante, escrita, null);
            resultado1.setEstudiante(estudiante);
            resultado1.setCompetencia(razonamiento);
            resultado1.setPuntaje(88.0);
            resultado1.setTipoPrueba("SABER_PRO");
            resultado1.setPuntajeGeneral(245.0);
            resultado1.setNumeroRegistro("REG-2024-001");
            resultado1.setNivel("4");
            resultado1.setBeneficios("Exoneración trabajo de grado con nota 5.0 + 100% beca derechos de grado");
            resultadoRepository.save(resultado1);
            
            Resultado resultado2 = new Resultado(estudiante, escrita, null);
            resultado2.setEstudiante(estudiante);
            resultado2.setCompetencia(lectura);
            resultado2.setPuntaje(92.0);
            resultado2.setTipoPrueba("SABER_PRO");
            resultado2.setPuntajeGeneral(245.0);
            resultado2.setNumeroRegistro("REG-2024-001");
            resultado2.setNivel("4");
            resultado2.setBeneficios("Exoneración trabajo de grado con nota 5.0 + 100% beca derechos de grado");
            resultadoRepository.save(resultado2);

            Resultado resultado3 = new Resultado(estudiante, escrita, null);
            resultado3.setEstudiante(estudiante);
            resultado3.setCompetencia(ingles);
            resultado3.setPuntaje(78.5);
            resultado3.setTipoPrueba("SABER_PRO");
            resultado3.setPuntajeGeneral(245.0);
            resultado3.setNumeroRegistro("REG-2024-001");
            resultado3.setNivel("4");
            resultado3.setBeneficios("Exoneración trabajo de grado con nota 5.0 + 100% beca derechos de grado");
            resultadoRepository.save(resultado3);

            Resultado resultado4 = new Resultado(estudiante, escrita, null);
            resultado4.setEstudiante(estudiante);
            resultado4.setCompetencia(ciudadanas);
            resultado4.setPuntaje(85.0);
            resultado4.setTipoPrueba("SABER_PRO");
            resultado4.setPuntajeGeneral(245.0);
            resultado4.setNumeroRegistro("REG-2024-001");
            resultado4.setNivel("4");
            resultado4.setBeneficios("Exoneración trabajo de grado con nota 5.0 + 100% beca derechos de grado");
            resultadoRepository.save(resultado4);

            Resultado resultado5 = new Resultado(estudiante, escrita, null);
            resultado5.setEstudiante(estudiante);
            resultado5.setCompetencia(escrita);
            resultado5.setPuntaje(90.5);
            resultado5.setTipoPrueba("SABER_PRO");
            resultado5.setPuntajeGeneral(245.0);
            resultado5.setNumeroRegistro("REG-2024-001");
            resultado5.setNivel("4");
            resultado5.setBeneficios("Exoneración trabajo de grado con nota 5.0 + 100% beca derechos de grado");
            resultadoRepository.save(resultado5);

            // Crear un segundo estudiante para pruebas
            Usuario estudianteUser2 = new Usuario();
            estudianteUser2.setDocumento("789");
            estudianteUser2.setPassword("estudiante456");
            estudianteUser2.setRol("ESTUDIANTE");
            usuarioRepository.save(estudianteUser2);

            Estudiante estudiante2 = new Estudiante();
            estudiante2.setTipoDocumento("CC");
            estudiante2.setNumeroDocumento("789");
            estudiante2.setNombres("Carlos");
            estudiante2.setApellidos("Rodríguez Méndez");
            estudiante2.setCorreo("carlos.rodriguez@email.com");
            estudiante2.setTelefono("3009876543");
            estudiante2.setActivo(true);
            estudiante2.setUsuario(estudianteUser2);
            estudiante2.setResultadosDisponibles(true);
            estudianteRepository.save(estudiante2);

            // Resultados para SABER_TT - Estudiante con nivel 3 (beneficio medio)
            Resultado resultado6 = new Resultado(estudiante2, escrita, null);
            resultado6.setEstudiante(estudiante2);
            resultado6.setCompetencia(razonamiento);
            resultado6.setPuntaje(75.0);
            resultado6.setTipoPrueba("SABER_TT");
            resultado6.setPuntajeGeneral(160.0);
            resultado6.setNumeroRegistro("REG-2024-002");
            resultado6.setNivel("3");
            resultado6.setBeneficios("Exoneración trabajo de grado con nota 4.7 + 50% beca derechos de grado");
            resultadoRepository.save(resultado6);

            Resultado resultado7 = new Resultado(estudiante2, escrita, null);
            resultado7.setEstudiante(estudiante2);
            resultado7.setCompetencia(lectura);
            resultado7.setPuntaje(82.0);
            resultado7.setTipoPrueba("SABER_TT");
            resultado7.setPuntajeGeneral(160.0);
            resultado7.setNumeroRegistro("REG-2024-002");
            resultado7.setNivel("3");
            resultado7.setBeneficios("Exoneración trabajo de grado con nota 4.7 + 50% beca derechos de grado");
            resultadoRepository.save(resultado7);
            
            System.out.println("✅ Resultados de prueba creados con niveles y beneficios");
            System.out.println("   - Estudiante Ana García: SABER_PRO, Nivel 4 (245 puntos)");
            System.out.println("   - Estudiante Carlos Rodríguez: SABER_TT, Nivel 3 (160 puntos)");
        }
        
        System.out.println("🎉 DATOS DE PRUEBA CARGADOS EXITOSAMENTE");
    }
}