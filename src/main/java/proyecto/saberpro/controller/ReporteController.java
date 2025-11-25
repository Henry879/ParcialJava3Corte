package proyecto.saberpro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.saberpro.model.Estudiante;
import proyecto.saberpro.model.Resultado;
import proyecto.saberpro.model.Usuario;
import proyecto.saberpro.repository.EstudianteRepository;
import proyecto.saberpro.repository.ResultadoRepository;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReporteController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ResultadoRepository resultadoRepository;

    @GetMapping("/reportes/estudiantes")
    public String reporteEstudiantes(
            @RequestParam(required = false) String filtroPuntaje,
            HttpSession session, 
            Model model) {
        
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        try {
            // Obtener todos los estudiantes activos
            List<Estudiante> estudiantes = estudianteRepository.findAll().stream()
                    .filter(Estudiante::isActivo)
                    .collect(Collectors.toList());

            // Lista para almacenar estudiantes con sus datos de resultados
            List<Map<String, Object>> estudiantesConResultados = new ArrayList<>();
            List<Double> todosPuntajes = new ArrayList<>();

            for (Estudiante estudiante : estudiantes) {
                // Buscar resultados del estudiante
                List<Resultado> resultados = resultadoRepository.findByEstudianteId(estudiante.getId());
                
                if (!resultados.isEmpty()) {
                    // Obtener datos del primer resultado (deberían ser consistentes)
                    Resultado primerResultado = resultados.get(0);
                    
                    // Calcular promedio de competencias
                    double promedioCompetencias = resultados.stream()
                            .mapToDouble(Resultado::getPuntaje)
                            .average()
                            .orElse(0.0);

                    Map<String, Object> datosEstudiante = new HashMap<>();
                    datosEstudiante.put("estudiante", estudiante);
                    datosEstudiante.put("puntajeGeneral", primerResultado.getPuntajeGeneral());
                    datosEstudiante.put("nivel", primerResultado.getNivel());
                    datosEstudiante.put("tipoPrueba", primerResultado.getTipoPrueba());
                    datosEstudiante.put("promedioCompetencias", Math.round(promedioCompetencias * 10.0) / 10.0);
                    datosEstudiante.put("totalCompetencias", resultados.size());
                    datosEstudiante.put("tieneResultados", true);
                    
                    estudiantesConResultados.add(datosEstudiante);
                    todosPuntajes.add(primerResultado.getPuntajeGeneral());
                } else {
                    // Estudiante sin resultados
                    Map<String, Object> datosEstudiante = new HashMap<>();
                    datosEstudiante.put("estudiante", estudiante);
                    datosEstudiante.put("puntajeGeneral", null);
                    datosEstudiante.put("nivel", "Sin resultados");
                    datosEstudiante.put("tipoPrueba", "N/A");
                    datosEstudiante.put("promedioCompetencias", 0.0);
                    datosEstudiante.put("totalCompetencias", 0);
                    datosEstudiante.put("tieneResultados", false);
                    
                    estudiantesConResultados.add(datosEstudiante);
                }
            }

            // Aplicar filtro si existe
            if (filtroPuntaje != null && !filtroPuntaje.isEmpty()) {
                estudiantesConResultados = filtrarPorPuntaje(estudiantesConResultados, filtroPuntaje);
            }

            // Ordenar por puntaje general descendente
            estudiantesConResultados.sort((a, b) -> {
                Double puntajeA = (Double) a.get("puntajeGeneral");
                Double puntajeB = (Double) b.get("puntajeGeneral");
                
                if (puntajeA == null && puntajeB == null) return 0;
                if (puntajeA == null) return 1;
                if (puntajeB == null) return -1;
                
                return puntajeB.compareTo(puntajeA);
            });

            // Calcular estadísticas generales
            Map<String, Object> estadisticas = calcularEstadisticas(estudiantesConResultados, todosPuntajes);

            model.addAttribute("estudiantesConResultados", estudiantesConResultados);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("filtroActual", filtroPuntaje != null ? filtroPuntaje : "todos");
            model.addAttribute("totalEstudiantes", estudiantes.size());

            return "reportes/lista-estudiantes";

        } catch (Exception e) {
            System.out.println("ERROR en reporteEstudiantes: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al generar el reporte: " + e.getMessage());
            return "reportes/lista-estudiantes";
        }
    }

    private List<Map<String, Object>> filtrarPorPuntaje(List<Map<String, Object>> estudiantes, String filtro) {
        return estudiantes.stream()
                .filter(est -> {
                    Double puntaje = (Double) est.get("puntajeGeneral");
                    if (puntaje == null) return false;

                    switch (filtro) {
                        case "bajo": return puntaje < 80;
                        case "medio-bajo": return puntaje >= 180 && puntaje <= 210;
                        case "medio-alto": return puntaje >= 211 && puntaje <= 240;
                        case "alto": return puntaje > 241;
                        case "sin-resultados": return puntaje == null;
                        default: return true;
                    }
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> calcularEstadisticas(List<Map<String, Object>> estudiantes, List<Double> todosPuntajes) {
        Map<String, Object> stats = new HashMap<>();

        // Filtrar estudiantes con resultados
        List<Map<String, Object>> conResultados = estudiantes.stream()
                .filter(est -> (Boolean) est.get("tieneResultados"))
                .collect(Collectors.toList());

        // Calcular estadísticas
        double promedio = todosPuntajes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double maximo = todosPuntajes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minimo = todosPuntajes.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        // Calcular mediana
        List<Double> puntajesOrdenados = todosPuntajes.stream().sorted().collect(Collectors.toList());
        double mediana = 0.0;
        if (!puntajesOrdenados.isEmpty()) {
            int middle = puntajesOrdenados.size() / 2;
            if (puntajesOrdenados.size() % 2 == 1) {
                mediana = puntajesOrdenados.get(middle);
            } else {
                mediana = (puntajesOrdenados.get(middle - 1) + puntajesOrdenados.get(middle)) / 2.0;
            }
        }

        // Contar estudiantes por nivel
        long nivel1 = conResultados.stream().filter(est -> "1".equals(est.get("nivel"))).count();
        long nivel2 = conResultados.stream().filter(est -> "2".equals(est.get("nivel"))).count();
        long nivel3 = conResultados.stream().filter(est -> "3".equals(est.get("nivel"))).count();
        long nivel4 = conResultados.stream().filter(est -> "4".equals(est.get("nivel"))).count();

        stats.put("promedio", Math.round(promedio * 10.0) / 10.0);
        stats.put("maximo", Math.round(maximo * 10.0) / 10.0);
        stats.put("minimo", Math.round(minimo * 10.0) / 10.0);
        stats.put("mediana", Math.round(mediana * 10.0) / 10.0);
        stats.put("totalConResultados", conResultados.size());
        stats.put("nivel1", nivel1);
        stats.put("nivel2", nivel2);
        stats.put("nivel3", nivel3);
        stats.put("nivel4", nivel4);

        return stats;
    }
}