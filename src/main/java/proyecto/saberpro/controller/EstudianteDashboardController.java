package proyecto.saberpro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import proyecto.saberpro.model.Estudiante;
import proyecto.saberpro.model.Resultado;
import proyecto.saberpro.model.Usuario;
import proyecto.saberpro.repository.EstudianteRepository;
import proyecto.saberpro.repository.ResultadoRepository;
import java.util.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
public class EstudianteDashboardController {	

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ResultadoRepository resultadoRepository;

    @GetMapping("/dashboard-estudiante")
    public String dashboardEstudiante(HttpSession session, Model model) {
        // Verificar que sea estudiante
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ESTUDIANTE")) {
            return "redirect:/login";
        }

        // Buscar el estudiante por documento
        Optional<Estudiante> estudianteOpt = estudianteRepository.findByNumeroDocumento(usuario.getDocumento());
        
        if (estudianteOpt.isEmpty()) {
            model.addAttribute("error", "No se encontró información del estudiante");
            return "dashboard-estudiante";
        }

        Estudiante estudiante = estudianteOpt.get();
        
        // Contar resultados del estudiante
        long totalResultados = resultadoRepository.countByEstudianteId(estudiante.getId());
        
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("totalResultados", totalResultados);
        
        return "dashboard-estudiante";
    }
    
    @GetMapping("/estudiante/resultados")
    public String verMisResultados(HttpSession session, Model model) {
        // Verificar que sea estudiante
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ESTUDIANTE")) {
            return "redirect:/login";
        }

        // Buscar el estudiante por documento
        Optional<Estudiante> estudianteOpt = estudianteRepository.findByNumeroDocumento(usuario.getDocumento());
        
        if (estudianteOpt.isEmpty()) {
            model.addAttribute("error", "No se encontró información del estudiante");
            return "estudiante/mis-resultados";
        }

        Estudiante estudiante = estudianteOpt.get();
        
        // Buscar todos los resultados del estudiante
        List<Resultado> resultados = resultadoRepository.findByEstudianteId(estudiante.getId());
        
        if (resultados.isEmpty()) {
            model.addAttribute("mensaje", "No hay resultados disponibles para mostrar");
        } else {
            // Calcular promedio de puntajes
            Double promedio = resultados.stream()
                .mapToDouble(Resultado::getPuntaje)
                .average()
                .orElse(0.0);
                
            // Obtener el primer resultado para datos generales (todos deberían ser iguales en estos campos)
            Resultado primerResultado = resultados.get(0);
            
            model.addAttribute("resultados", resultados);
            model.addAttribute("promedio", Math.round(promedio * 10.0) / 10.0); // Redondear a 1 decimal
            model.addAttribute("puntajeGeneral", primerResultado.getPuntajeGeneral());
            model.addAttribute("tipoPrueba", primerResultado.getTipoPrueba());
            model.addAttribute("numeroRegistro", primerResultado.getNumeroRegistro());
            model.addAttribute("nivelGeneral", primerResultado.getNivel());
            model.addAttribute("beneficios", primerResultado.getBeneficios());
        }
        
        model.addAttribute("estudiante", estudiante);
        return "estudiante/mis-resultados";
        
        
    }
    @GetMapping("/estudiante/estadisticas")
    public String verEstadisticas(HttpSession session, Model model) {
        // Verificar que sea estudiante
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ESTUDIANTE")) {
            return "redirect:/login";
        }

        try {
            // Buscar el estudiante por documento
            Optional<Estudiante> estudianteOpt = estudianteRepository.findByNumeroDocumento(usuario.getDocumento());
            
            if (estudianteOpt.isEmpty()) {
                model.addAttribute("error", "No se encontró información del estudiante");
                return "estudiante/estadisticas";
            }

            Estudiante estudiante = estudianteOpt.get();
            
            // Buscar resultados del estudiante actual
            List<Resultado> misResultados = resultadoRepository.findByEstudianteId(estudiante.getId());
            
            if (misResultados.isEmpty()) {
                model.addAttribute("mensaje", "No tienes resultados disponibles para comparar");
                model.addAttribute("estudiante", estudiante);
                return "estudiante/estadisticas";
            }

            // Obtener todos los resultados para calcular estadísticas
            List<Resultado> todosResultados = resultadoRepository.findAll();
            
            // Calcular estadísticas
            Map<String, Object> estadisticas = calcularEstadisticas(misResultados, todosResultados, estudiante);
            
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("misResultados", misResultados);
            
            return "estudiante/estadisticas";
            
        } catch (Exception e) {
            System.out.println("ERROR en verEstadisticas: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar estadísticas: " + e.getMessage());
            return "estudiante/estadisticas";
        }
    }

    private Map<String, Object> calcularEstadisticas(List<Resultado> misResultados, List<Resultado> todosResultados, Estudiante estudiante) {
        Map<String, Object> stats = new HashMap<>();
        
        // Estadísticas generales del grupo
        long totalEstudiantes = todosResultados.stream()
            .map(r -> r.getEstudiante().getId())
            .distinct()
            .count();
        
        // Agrupar por competencia para calcular promedios
        Map<String, List<Double>> puntajesPorCompetencia = new HashMap<>();
        Map<String, Double> promediosCompetencia = new HashMap<>();
        Map<String, Double> maximosCompetencia = new HashMap<>();
        Map<String, Double> minimosCompetencia = new HashMap<>();
        
        for (Resultado resultado : todosResultados) {
            String competencia = resultado.getCompetencia().getNombre();
            double puntaje = resultado.getPuntaje();
            
            puntajesPorCompetencia.computeIfAbsent(competencia, k -> new ArrayList<>()).add(puntaje);
        }
        
        // Calcular promedios, máximos y mínimos por competencia
        for (Map.Entry<String, List<Double>> entry : puntajesPorCompetencia.entrySet()) {
            String competencia = entry.getKey();
            List<Double> puntajes = entry.getValue();
            
            double promedio = puntajes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double maximo = puntajes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double minimo = puntajes.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            
            promediosCompetencia.put(competencia, Math.round(promedio * 10.0) / 10.0);
            maximosCompetencia.put(competencia, Math.round(maximo * 10.0) / 10.0);
            minimosCompetencia.put(competencia, Math.round(minimo * 10.0) / 10.0);
        }
        
        // Calcular percentil del estudiante
        double miPromedio = misResultados.stream()
            .mapToDouble(Resultado::getPuntaje)
            .average()
            .orElse(0.0);
        
        // Simular cálculo de percentil (en un sistema real sería más complejo)
        String percentil = calcularPercentil(miPromedio);
        
        stats.put("totalEstudiantes", totalEstudiantes);
        stats.put("promediosCompetencia", promediosCompetencia);
        stats.put("maximosCompetencia", maximosCompetencia);
        stats.put("minimosCompetencia", minimosCompetencia);
        stats.put("miPromedio", Math.round(miPromedio * 10.0) / 10.0);
        stats.put("percentil", percentil);
        stats.put("competenciasConDatos", new ArrayList<>(promediosCompetencia.keySet()));
        
        return stats;
    }

    private String calcularPercentil(double miPromedio) {
        // Simulación simple de percentil basado en el promedio
        if (miPromedio >= 90) return "Top 10%";
        else if (miPromedio >= 80) return "Top 25%";
        else if (miPromedio >= 70) return "Top 50%";
        else if (miPromedio >= 60) return "Top 75%";
        else return "Top 100%";
    }
 // AGREGAR ESTE MÉTODO AL FINAL DE EstudianteDashboardController.java

    @GetMapping("/estudiante/beneficios")
    public String verMisBeneficios(HttpSession session, Model model) {
        // Verificar que sea estudiante
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ESTUDIANTE")) {
            return "redirect:/login";
        }

        try {
            // Buscar el estudiante por documento
            Optional<Estudiante> estudianteOpt = estudianteRepository.findByNumeroDocumento(usuario.getDocumento());
            
            if (estudianteOpt.isEmpty()) {
                model.addAttribute("error", "No se encontró información del estudiante");
                return "estudiante/beneficios";
            }

            Estudiante estudiante = estudianteOpt.get();
            
            // Buscar todos los resultados del estudiante
            List<Resultado> resultados = resultadoRepository.findByEstudianteId(estudiante.getId());
            
            if (resultados.isEmpty()) {
                model.addAttribute("mensaje", "No hay resultados disponibles para mostrar beneficios");
            } else {
                // Obtener el primer resultado para datos generales
                Resultado primerResultado = resultados.get(0);
                
                // Verificar si hay alerta de graduación en algún resultado
                boolean tieneAlertaGraduacion = resultados.stream()
                    .anyMatch(r -> r.getAlertaGraduacion() != null && r.getAlertaGraduacion());
                
                // Calcular estadísticas de beneficios
                long totalConExoneracion = resultados.stream()
                    .filter(r -> r.getExoneracionNota() != null && !"0".equals(r.getExoneracionNota()))
                    .count();
                    
                long totalConBeca = resultados.stream()
                    .filter(r -> r.getBecaPorcentaje() != null && !"0".equals(r.getBecaPorcentaje()))
                    .count();
                
                model.addAttribute("resultados", resultados);
                model.addAttribute("estudiante", estudiante);
                model.addAttribute("puntajeGeneral", primerResultado.getPuntajeGeneral());
                model.addAttribute("tipoPrueba", primerResultado.getTipoPrueba());
                model.addAttribute("nivelGeneral", primerResultado.getNivel());
                model.addAttribute("beneficiosGeneral", primerResultado.getBeneficios());
                model.addAttribute("tieneAlertaGraduacion", tieneAlertaGraduacion);
                model.addAttribute("totalConExoneracion", totalConExoneracion);
                model.addAttribute("totalConBeca", totalConBeca);
                model.addAttribute("exoneracionNota", primerResultado.getExoneracionNota());
                model.addAttribute("becaPorcentaje", primerResultado.getBecaPorcentaje());
            }
            
            return "estudiante/beneficios";
            
        } catch (Exception e) {
            System.out.println("ERROR en verMisBeneficios: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los beneficios: " + e.getMessage());
            return "estudiante/beneficios";
        }
    }
}