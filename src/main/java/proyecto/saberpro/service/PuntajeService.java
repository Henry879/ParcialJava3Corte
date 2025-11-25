package proyecto.saberpro.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class PuntajeService {
    
    public Map<String, String> calcularNivelYBeneficios(String tipoPrueba, Double puntajeGeneral) {
        Map<String, String> resultado = new HashMap<>();
        
        if (puntajeGeneral == null) {
            resultado.put("nivel", "1");
            resultado.put("beneficios", "Sin beneficios - Puntaje no válido");
            return resultado;
        }
        
        if ("SABER_PRO".equals(tipoPrueba)) {
            if (puntajeGeneral >= 241) {
                resultado.put("nivel", "4");
                resultado.put("beneficios", "Exoneración trabajo de grado con nota 5.0 + 100% beca derechos de grado");
            } else if (puntajeGeneral >= 211) {
                resultado.put("nivel", "3");
                resultado.put("beneficios", "Exoneración trabajo de grado con nota 4.7 + 50% beca derechos de grado");
            } else if (puntajeGeneral >= 180) {
                resultado.put("nivel", "2");
                resultado.put("beneficios", "Exoneración trabajo de grado con nota 4.5");
            } else {
                resultado.put("nivel", "1");
                resultado.put("beneficios", "Sin beneficios");
            }
        } else if ("SABER_TT".equals(tipoPrueba)) {
            if (puntajeGeneral >= 171) {
                resultado.put("nivel", "4");
                resultado.put("beneficios", "Exoneración trabajo de grado con nota 5.0 + 100% beca derechos de grado");
            } else if (puntajeGeneral >= 151) {
                resultado.put("nivel", "3");
                resultado.put("beneficios", "Exoneración trabajo de grado con nota 4.7 + 50% beca derechos de grado");
            } else if (puntajeGeneral >= 120) {
                resultado.put("nivel", "2");
                resultado.put("beneficios", "Exoneración trabajo de grado con nota 4.5");
            } else {
                resultado.put("nivel", "1");
                resultado.put("beneficios", "Sin beneficios");
            }
        } else {
            resultado.put("nivel", "1");
            resultado.put("beneficios", "Sin beneficios - Tipo de prueba no válido");
        }
        
        return resultado;
    }

    public boolean validarRangoPuntaje(String tipoPrueba, Double puntaje) {
        if (puntaje == null) return false;
        
        if ("SABER_PRO".equals(tipoPrueba)) {
            return puntaje >= 0 && puntaje <= 300;
        } else if ("SABER_TT".equals(tipoPrueba)) {
            return puntaje >= 0 && puntaje <= 200;
        }
        return false;
    }

    public String getColorNivel(String nivel) {
        switch(nivel) {
            case "1": return "rojo";
            case "2": return "amarillo"; 
            case "3": return "verde";
            case "4": return "azul";
            default: return "gris";
        }
    }
}