package proyecto.saberpro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.saberpro.model.Estudiante;
import proyecto.saberpro.model.Usuario;
import proyecto.saberpro.repository.EstudianteRepository;
import proyecto.saberpro.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // LISTAR TODOS LOS ESTUDIANTES
    @GetMapping
    public String listarEstudiantes(HttpSession session, Model model) {
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        List<Estudiante> estudiantes = estudianteRepository.findAll();
        model.addAttribute("estudiantes", estudiantes);
        return "gestion/lista-estudiantes";  // ✅ CORREGIDO
    }

    // MOSTRAR FORMULARIO DE NUEVO ESTUDIANTE
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(HttpSession session, Model model) {
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        model.addAttribute("estudiante", new Estudiante());
        return "gestion/formulario-estudiante";  // ✅ CORREGIDO
    }

    // GUARDAR NUEVO ESTUDIANTE
    @PostMapping("/guardar")
    public String guardarEstudiante(@ModelAttribute Estudiante estudiante,
                                   HttpSession session,
                                   Model model) {
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        try {
            // Verificar si ya existe un estudiante con ese documento
            Optional<Estudiante> existente = estudianteRepository.findByNumeroDocumento(estudiante.getNumeroDocumento());
            if (existente.isPresent()) {
                model.addAttribute("error", "Ya existe un estudiante con el documento: " + estudiante.getNumeroDocumento());
                model.addAttribute("estudiante", estudiante);
                return "gestion/formulario-estudiante";  // ✅ CORREGIDO
            }

            // Crear usuario para el estudiante
            Usuario usuarioEstudiante = new Usuario();
            usuarioEstudiante.setDocumento(estudiante.getNumeroDocumento());
            usuarioEstudiante.setPassword("estudiante123"); // Contraseña por defecto
            usuarioEstudiante.setRol("ESTUDIANTE");
            usuarioRepository.save(usuarioEstudiante);

            // Asignar usuario al estudiante
            estudiante.setUsuario(usuarioEstudiante);
            estudiante.setActivo(true);
            estudiante.setResultadosDisponibles(false);

            estudianteRepository.save(estudiante);

            model.addAttribute("success", "Estudiante creado exitosamente!");
            return "redirect:/estudiantes";

        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el estudiante: " + e.getMessage());
            model.addAttribute("estudiante", estudiante);
            return "gestion/formulario-estudiante";  // ✅ CORREGIDO
        }
    }

    // MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, HttpSession session, Model model) {
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        Optional<Estudiante> estudianteOpt = estudianteRepository.findById(id);
        if (estudianteOpt.isEmpty()) {
            model.addAttribute("error", "Estudiante no encontrado");
            return "redirect:/estudiantes";
        }

        model.addAttribute("estudiante", estudianteOpt.get());
        return "gestion/formulario-estudiante";  // ✅ CORREGIDO
    }

    // ACTUALIZAR ESTUDIANTE
    @PostMapping("/actualizar/{id}")
    public String actualizarEstudiante(@PathVariable Long id,
                                      @ModelAttribute Estudiante estudianteActualizado,
                                      HttpSession session,
                                      Model model) {
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        try {
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(id);
            if (estudianteOpt.isEmpty()) {
                model.addAttribute("error", "Estudiante no encontrado");
                return "redirect:/estudiantes";
            }

            Estudiante estudiante = estudianteOpt.get();
            
            // Actualizar campos
            estudiante.setTipoDocumento(estudianteActualizado.getTipoDocumento());
            estudiante.setNombres(estudianteActualizado.getNombres());
            estudiante.setApellidos(estudianteActualizado.getApellidos());
            estudiante.setCorreo(estudianteActualizado.getCorreo());
            estudiante.setTelefono(estudianteActualizado.getTelefono());
            estudiante.setActivo(estudianteActualizado.isActivo());
            estudiante.setResultadosDisponibles(estudianteActualizado.isResultadosDisponibles());

            estudianteRepository.save(estudiante);

            model.addAttribute("success", "Estudiante actualizado exitosamente!");
            return "redirect:/estudiantes";

        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el estudiante: " + e.getMessage());
            model.addAttribute("estudiante", estudianteActualizado);
            return "gestion/formulario-estudiante";  // ✅ CORREGIDO
        }
    }

    // CAMBIAR ESTADO (ACTIVAR/DESACTIVAR)
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, HttpSession session, Model model) {
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        try {
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(id);
            if (estudianteOpt.isEmpty()) {
                model.addAttribute("error", "Estudiante no encontrado");
                return "redirect:/estudiantes";
            }

            Estudiante estudiante = estudianteOpt.get();
            estudiante.setActivo(!estudiante.isActivo());
            estudianteRepository.save(estudiante);

            String estado = estudiante.isActivo() ? "activado" : "desactivado";
            model.addAttribute("success", "Estudiante " + estado + " exitosamente!");

        } catch (Exception e) {
            model.addAttribute("error", "Error al cambiar estado: " + e.getMessage());
        }

        return "redirect:/estudiantes";
    }

    // ELIMINAR ESTUDIANTE
    @PostMapping("/eliminar/{id}")
    public String eliminarEstudiante(@PathVariable Long id, HttpSession session, Model model) {
        // Verificar que sea coordinador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }

        try {
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(id);
            if (estudianteOpt.isEmpty()) {
                model.addAttribute("error", "Estudiante no encontrado");
                return "redirect:/estudiantes";
            }

            Estudiante estudiante = estudianteOpt.get();
            
            // Verificar si tiene resultados antes de eliminar
            // (En un sistema real, considerar eliminación lógica en lugar de física)
            
            estudianteRepository.delete(estudiante);
            model.addAttribute("success", "Estudiante eliminado exitosamente!");

        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar estudiante: " + e.getMessage());
        }

        return "redirect:/estudiantes";
    }
}