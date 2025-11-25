package proyecto.saberpro.controller;

import proyecto.saberpro.model.Usuario;
import proyecto.saberpro.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";  // ← Redirige a login desde la raíz
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";  // ← Esto busca src/main/resources/templates/login.html
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String documento,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        Usuario usuario = usuarioRepository.findByDocumento(documento);

        if (usuario == null || !usuario.getPassword().equals(password)) {
            model.addAttribute("error", "Documento o contraseña incorrectos");
            return "login";
        }

        session.setAttribute("usuario", usuario);

        if (usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/dashboard-coordinador";
        } else {
            return "redirect:/dashboard-estudiante";
        }
    }

    @GetMapping("/dashboard-coordinador")
    public String dashboardCoordinador(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("COORDINADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "dashboard-coordinador";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}