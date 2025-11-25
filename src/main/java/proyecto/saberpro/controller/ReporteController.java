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

            return "reportes/reporte-estudiantes";

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
                    if (puntaje == null) return filtro.equals("sin-resultados");

                    switch (filtro) {
                        case "bajo": return puntaje < 100;
                        case "medio-bajo": return puntaje >= 100 && puntaje < 140;
                        case "medio-alto": return puntaje >= 140 && puntaje < 170;
                        case "alto": return puntaje >= 170;
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

    @GetMapping("/reportes/beneficios")
    public String reporteBeneficios(
            @RequestParam(required = false) String filtroRango,
            HttpSession session, Model model) {
        
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

            // Lista para estudiantes con beneficios
            List<Map<String, Object>> estudiantesConBeneficios = new ArrayList<>();
            List<Double> todosPuntajes = new ArrayList<>();

            for (Estudiante estudiante : estudiantes) {
                // Buscar resultados del estudiante
                List<Resultado> resultados = resultadoRepository.findByEstudianteId(estudiante.getId());
                
                if (!resultados.isEmpty()) {
                    // Obtener datos del primer resultado
                    Resultado primerResultado = resultados.get(0);
                    
                    // Calcular si tiene beneficios
                    boolean tieneBeneficios = !"0".equals(primerResultado.getExoneracionNota());
                    boolean tieneAlerta = primerResultado.getAlertaGraduacion() != null && 
                                         primerResultado.getAlertaGraduacion();
                    
                    Map<String, Object> datosEstudiante = new HashMap<>();
                    datosEstudiante.put("estudiante", estudiante);
                    datosEstudiante.put("puntajeGeneral", primerResultado.getPuntajeGeneral());
                    datosEstudiante.put("nivel", primerResultado.getNivel());
                    datosEstudiante.put("tipoPrueba", primerResultado.getTipoPrueba());
                    datosEstudiante.put("beneficios", primerResultado.getBeneficios());
                    datosEstudiante.put("exoneracionNota", primerResultado.getExoneracionNota());
                    datosEstudiante.put("becaPorcentaje", primerResultado.getBecaPorcentaje());
                    datosEstudiante.put("tieneBeneficios", tieneBeneficios);
                    datosEstudiante.put("tieneAlerta", tieneAlerta);
                    datosEstudiante.put("tieneResultados", true);
                    
                    estudiantesConBeneficios.add(datosEstudiante);
                    todosPuntajes.add(primerResultado.getPuntajeGeneral());
                } else {
                    // Estudiante sin resultados
                    Map<String, Object> datosEstudiante = new HashMap<>();
                    datosEstudiante.put("estudiante", estudiante);
                    datosEstudiante.put("puntajeGeneral", null);
                    datosEstudiante.put("nivel", "Sin resultados");
                    datosEstudiante.put("tipoPrueba", "N/A");
                    datosEstudiante.put("beneficios", "Sin datos");
                    datosEstudiante.put("exoneracionNota", "0");
                    datosEstudiante.put("becaPorcentaje", "0");
                    datosEstudiante.put("tieneBeneficios", false);
                    datosEstudiante.put("tieneAlerta", false);
                    datosEstudiante.put("tieneResultados", false);
                    
                    estudiantesConBeneficios.add(datosEstudiante);
                }
            }

            // Aplicar filtro por rango si existe
            if (filtroRango != null && !filtroRango.isEmpty()) {
                estudiantesConBeneficios = filtrarPorRangoBeneficios(estudiantesConBeneficios, filtroRango);
            }

            // Ordenar por puntaje general descendente
            estudiantesConBeneficios.sort((a, b) -> {
                Double puntajeA = (Double) a.get("puntajeGeneral");
                Double puntajeB = (Double) b.get("puntajeGeneral");
                
                if (puntajeA == null && puntajeB == null) return 0;
                if (puntajeA == null) return 1;
                if (puntajeB == null) return -1;
                
                return puntajeB.compareTo(puntajeA);
            });

            // Calcular estadísticas de beneficios
            Map<String, Object> estadisticasBeneficios = calcularEstadisticasBeneficios(estudiantesConBeneficios);

            model.addAttribute("estudiantesConBeneficios", estudiantesConBeneficios);
            model.addAttribute("estadisticas", estadisticasBeneficios);
            model.addAttribute("filtroActual", filtroRango != null ? filtroRango : "todos");
            model.addAttribute("totalEstudiantes", estudiantes.size());

            return "reportes/reporte-beneficios";

        } catch (Exception e) {
            System.out.println("ERROR en reporteBeneficios: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al generar el reporte de beneficios: " + e.getMessage());
            return "reportes/reporte-beneficios";
        }
    }

    private List<Map<String, Object>> filtrarPorRangoBeneficios(List<Map<String, Object>> estudiantes, String filtro) {
        return estudiantes.stream()
                .filter(est -> {
                    Double puntaje = (Double) est.get("puntajeGeneral");
                    if (puntaje == null) return filtro.equals("sin-resultados");

                    switch (filtro) {
                        case "alto": return puntaje >= 170;
                        case "medio-alto": return puntaje >= 140 && puntaje < 170;
                        case "medio-bajo": return puntaje >= 100 && puntaje < 140;
                        case "bajo": return puntaje < 100;
                        case "con-beneficios": return !"0".equals(est.get("exoneracionNota"));
                        case "con-alerta": return (Boolean) est.get("tieneAlerta");
                        case "sin-resultados": return puntaje == null;
                        default: return true;
                    }
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> calcularEstadisticasBeneficios(List<Map<String, Object>> estudiantes) {
        Map<String, Object> stats = new HashMap<>();

        // Filtrar estudiantes con resultados
        List<Map<String, Object>> conResultados = estudiantes.stream()
                .filter(est -> (Boolean) est.get("tieneResultados"))
                .collect(Collectors.toList());

        // Calcular estadísticas básicas
        List<Double> puntajes = conResultados.stream()
                .map(est -> (Double) est.get("puntajeGeneral"))
                .collect(Collectors.toList());

        double promedio = puntajes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double maximo = puntajes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minimo = puntajes.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        // Contar por tipo de beneficio
        long conExoneracion = conResultados.stream()
                .filter(est -> !"0".equals(est.get("exoneracionNota")))
                .count();
                
        long conBeca100 = conResultados.stream()
                .filter(est -> "100".equals(est.get("becaPorcentaje")))
                .count();
                
        long conBeca50 = conResultados.stream()
                .filter(est -> "50".equals(est.get("becaPorcentaje")))
                .count();
                
        long conAlerta = conResultados.stream()
                .filter(est -> (Boolean) est.get("tieneAlerta"))
                .count();

        stats.put("promedio", Math.round(promedio * 10.0) / 10.0);
        stats.put("maximo", Math.round(maximo * 10.0) / 10.0);
        stats.put("minimo", Math.round(minimo * 10.0) / 10.0);
        stats.put("totalConResultados", conResultados.size());
        stats.put("conExoneracion", conExoneracion);
        stats.put("conBeca100", conBeca100);
        stats.put("conBeca50", conBeca50);
        stats.put("conAlerta", conAlerta);
        stats.put("sinBeneficios", conResultados.size() - conExoneracion);

        return stats;
    }
 // AGREGAR ESTOS MÉTODOS AL FINAL DE ReporteController.java

    @GetMapping("/reportes/competencias-completo")
    public String reporteCompetenciasCompleto(HttpSession session, Model model) {
        
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        try {
            // Obtener todos los estudiantes activos con resultados
            List<Estudiante> estudiantes = estudianteRepository.findAll().stream()
                    .filter(Estudiante::isActivo)
                    .filter(e -> !resultadoRepository.findByEstudianteId(e.getId()).isEmpty())
                    .collect(Collectors.toList());

            // Obtener todas las competencias
            List<proyecto.saberpro.model.Competencia> competencias = Arrays.asList(
                new proyecto.saberpro.model.Competencia("Comunicación Escrita", ""),
                new proyecto.saberpro.model.Competencia("Razonamiento Cuantitativo", ""),
                new proyecto.saberpro.model.Competencia("Lectura Crítica", ""),
                new proyecto.saberpro.model.Competencia("Competencias Ciudadanas", ""),
                new proyecto.saberpro.model.Competencia("Inglés", ""),
                new proyecto.saberpro.model.Competencia("Formulación De Proyectos De Ingeniería", ""),
                new proyecto.saberpro.model.Competencia("Pensamiento Científico - Matemáticas Y Estadística", ""),
                new proyecto.saberpro.model.Competencia("Diseño De Software", "")
            );

            // Lista para datos completos de estudiantes
            List<Map<String, Object>> estudiantesCompletos = new ArrayList<>();
            
            // Estadísticas por competencia
            Map<String, List<Double>> puntajesPorCompetencia = new HashMap<>();
            Map<String, Object> estadisticasCompetencias = new HashMap<>();

            for (Estudiante estudiante : estudiantes) {
                // Buscar todos los resultados del estudiante
                List<Resultado> resultados = resultadoRepository.findByEstudianteId(estudiante.getId());
                
                if (!resultados.isEmpty()) {
                    Map<String, Object> datosEstudiante = new HashMap<>();
                    datosEstudiante.put("estudiante", estudiante);
                    datosEstudiante.put("puntajeGeneral", resultados.get(0).getPuntajeGeneral());
                    datosEstudiante.put("nivelGeneral", resultados.get(0).getNivel());
                    datosEstudiante.put("tipoPrueba", resultados.get(0).getTipoPrueba());
                    
                    // Datos por competencia
                    Map<String, Map<String, Object>> competenciasData = new HashMap<>();
                    
                    for (Resultado resultado : resultados) {
                        String nombreCompetencia = resultado.getCompetencia().getNombre();
                        double puntaje = resultado.getPuntaje();
                        String nivel = resultado.getNivel();
                        
                        // Almacenar datos de la competencia
                        Map<String, Object> competenciaData = new HashMap<>();
                        competenciaData.put("puntaje", puntaje);
                        competenciaData.put("nivel", nivel);
                        competenciaData.put("beneficios", resultado.getBeneficios());
                        
                        competenciasData.put(nombreCompetencia, competenciaData);
                        
                        // Acumular para estadísticas
                        puntajesPorCompetencia.computeIfAbsent(nombreCompetencia, k -> new ArrayList<>()).add(puntaje);
                    }
                    
                    datosEstudiante.put("competencias", competenciasData);
                    estudiantesCompletos.add(datosEstudiante);
                }
            }

            // Calcular estadísticas por competencia
            for (Map.Entry<String, List<Double>> entry : puntajesPorCompetencia.entrySet()) {
                String competencia = entry.getKey();
                List<Double> puntajes = entry.getValue();
                
                double promedio = puntajes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double maximo = puntajes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                double minimo = puntajes.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                
                Map<String, Object> statsCompetencia = new HashMap<>();
                statsCompetencia.put("promedio", Math.round(promedio * 10.0) / 10.0);
                statsCompetencia.put("maximo", Math.round(maximo * 10.0) / 10.0);
                statsCompetencia.put("minimo", Math.round(minimo * 10.0) / 10.0);
                statsCompetencia.put("totalEstudiantes", puntajes.size());
                
                estadisticasCompetencias.put(competencia, statsCompetencia);
            }

            // Identificar fortalezas y debilidades
            List<Map<String, Object>> fortalezas = identificarFortalezasDebilidades(estadisticasCompetencias, "fortaleza");
            List<Map<String, Object>> debilidades = identificarFortalezasDebilidades(estadisticasCompetencias, "debilidad");

            model.addAttribute("estudiantesCompletos", estudiantesCompletos);
            model.addAttribute("competencias", competencias);
            model.addAttribute("estadisticasCompetencias", estadisticasCompetencias);
            model.addAttribute("fortalezas", fortalezas);
            model.addAttribute("debilidades", debilidades);
            model.addAttribute("totalEstudiantes", estudiantes.size());

            return "reportes/reporte-competencias-completo";

        } catch (Exception e) {
            System.out.println("ERROR en reporteCompetenciasCompleto: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al generar el reporte de competencias: " + e.getMessage());
            return "reportes/reporte-competencias-completo";
        }
    }

    private List<Map<String, Object>> identificarFortalezasDebilidades(Map<String, Object> estadisticas, String tipo) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        // Calcular promedio general de todas las competencias
        double promedioGeneral = estadisticas.values().stream()
                .mapToDouble(stat -> (Double) ((Map<String, Object>) stat).get("promedio"))
                .average()
                .orElse(0.0);
        
        for (Map.Entry<String, Object> entry : estadisticas.entrySet()) {
            String competencia = entry.getKey();
            Map<String, Object> stats = (Map<String, Object>) entry.getValue();
            double promedio = (Double) stats.get("promedio");
            
            if (tipo.equals("fortaleza") && promedio > promedioGeneral + 5) {
                Map<String, Object> fortaleza = new HashMap<>();
                fortaleza.put("competencia", competencia);
                fortaleza.put("promedio", promedio);
                fortaleza.put("diferencia", Math.round((promedio - promedioGeneral) * 10.0) / 10.0);
                resultados.add(fortaleza);
            } else if (tipo.equals("debilidad") && promedio < promedioGeneral - 5) {
                Map<String, Object> debilidad = new HashMap<>();
                debilidad.put("competencia", competencia);
                debilidad.put("promedio", promedio);
                debilidad.put("diferencia", Math.round((promedioGeneral - promedio) * 10.0) / 10.0);
                resultados.add(debilidad);
            }
        }
        
        // Ordenar por diferencia
        resultados.sort((a, b) -> {
            Double diffA = (Double) a.get("diferencia");
            Double diffB = (Double) b.get("diferencia");
            return diffB.compareTo(diffA);
        });
        
        return resultados;
    }
}