package proyecto.saberpro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.saberpro.model.*;
import proyecto.saberpro.repository.*;
import proyecto.saberpro.service.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/resultados")
public class ResultadoController {

    @Autowired
    private ResultadoRepository resultadoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CompetenciaRepository competenciaRepository;

    @Autowired
    private PuntajeService puntajeService;

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        List<Estudiante> estudiantes = estudianteRepository.findAll();
        List<Competencia> competencias = competenciaRepository.findAll();

        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("competencias", competencias);
        model.addAttribute("resultado", new Resultado());
        
        return "resultados/ingresar-resultado";
    }

    @PostMapping("/guardar")
    public String guardarResultado(
            @RequestParam Long estudianteId,
            @RequestParam Long competenciaId,
            @RequestParam Double puntaje,
            @RequestParam String tipoPrueba,
            @RequestParam Double puntajeGeneral,
            @RequestParam String numeroRegistro,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        try {
            if (!puntajeService.validarRangoPuntaje(tipoPrueba, puntajeGeneral)) {
                model.addAttribute("error", "El puntaje general no está en el rango válido para " + tipoPrueba);
                return cargarFormularioConDatos(model);
            }

            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(estudianteId);
            Optional<Competencia> competenciaOpt = competenciaRepository.findById(competenciaId);

            if (estudianteOpt.isEmpty() || competenciaOpt.isEmpty()) {
                model.addAttribute("error", "Estudiante o competencia no encontrados");
                return cargarFormularioConDatos(model);
            }

            // ✅ USAR NUEVO CÁLCULO SEGÚN ACUERDO 01-009
            Map<String, Object> calculo = puntajeService.calcularBeneficiosSegunAcuerdo(tipoPrueba, puntajeGeneral);
            String nivel = (String) calculo.get("nivel");
            String beneficios = (String) calculo.get("beneficios");
            Boolean alertaGraduacion = (Boolean) calculo.get("alerta_graduacion");

            Resultado resultado = new Resultado();
            resultado.setEstudiante(estudianteOpt.get());
            resultado.setCompetencia(competenciaOpt.get());
            resultado.setPuntaje(puntaje);
            resultado.setTipoPrueba(tipoPrueba);
            resultado.setPuntajeGeneral(puntajeGeneral);
            resultado.setNumeroRegistro(numeroRegistro);
            resultado.setNivel(nivel);
            resultado.setBeneficios(beneficios);
            resultado.setExoneracionNota((String) calculo.get("exoneracion_nota"));
            resultado.setBecaPorcentaje((String) calculo.get("beca_porcentaje"));
            resultado.setAlertaGraduacion(alertaGraduacion);

            resultadoRepository.save(resultado);

            model.addAttribute("success", "Resultado guardado exitosamente!");
            model.addAttribute("nivelCalculado", nivel);
            model.addAttribute("beneficiosCalculados", beneficios);
            model.addAttribute("colorNivel", puntajeService.getColorNivel(nivel));
            model.addAttribute("alertaGraduacion", alertaGraduacion);

        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el resultado: " + e.getMessage());
        }

        return cargarFormularioConDatos(model);
    }

    @GetMapping
    public String listarResultados(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        List<Resultado> resultados = resultadoRepository.findAll();
        model.addAttribute("resultados", resultados);
        return "resultados/lista-resultados";
    }

    private String cargarFormularioConDatos(Model model) {
        List<Estudiante> estudiantes = estudianteRepository.findAll();
        List<Competencia> competencias = competenciaRepository.findAll();
        
        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("competencias", competencias);
        model.addAttribute("resultado", new Resultado());
        
        return "resultados/ingresar-resultado";
    }
}