package proyecto.saberpro.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class PuntajeService {
    
    public Map<String, Object> calcularBeneficiosSegunAcuerdo(String tipoPrueba, Double puntajeGeneral) {
        Map<String, Object> resultado = new HashMap<>();
        
        if (puntajeGeneral == null) {
            return crearResultadoError("Puntaje no válido");
        }
        
        if ("SABER_PRO".equals(tipoPrueba)) {
            return calcularBeneficiosSaberPro(puntajeGeneral);
        } else if ("SABER_TT".equals(tipoPrueba)) {
            return calcularBeneficiosSaberTT(puntajeGeneral);
        } else {
            return crearResultadoError("Tipo de prueba no válido");
        }
    }
    
    private Map<String, Object> calcularBeneficiosSaberPro(Double puntaje) {
        Map<String, Object> beneficios = new HashMap<>();
        
        if (puntaje >= 241) {
            beneficios.put("nivel", "4");
            beneficios.put("beneficios", "Exoneración con nota 5.0 + 100% beca derechos de grado");
            beneficios.put("exoneracion_nota", "5.0");
            beneficios.put("beca_porcentaje", "100");
            beneficios.put("alerta_graduacion", false);
        } else if (puntaje >= 211) {
            beneficios.put("nivel", "3");
            beneficios.put("beneficios", "Exoneración con nota 4.7 + 50% beca derechos de grado");
            beneficios.put("exoneracion_nota", "4.7");
            beneficios.put("beca_porcentaje", "50");
            beneficios.put("alerta_graduacion", false);
        } else if (puntaje >= 180) {
            beneficios.put("nivel", "2");
            beneficios.put("beneficios", "Exoneración con nota 4.5");
            beneficios.put("exoneracion_nota", "4.5");
            beneficios.put("beca_porcentaje", "0");
            beneficios.put("alerta_graduacion", false);
        } else if (puntaje < 80) {
            beneficios.put("nivel", "1");
            beneficios.put("beneficios", "ALERTA: Riesgo de no graduación");
            beneficios.put("exoneracion_nota", "0");
            beneficios.put("beca_porcentaje", "0");
            beneficios.put("alerta_graduacion", true);
        } else {
            beneficios.put("nivel", "1");
            beneficios.put("beneficios", "Sin beneficios aplicables");
            beneficios.put("exoneracion_nota", "0");
            beneficios.put("beca_porcentaje", "0");
            beneficios.put("alerta_graduacion", false);
        }
        
        return beneficios;
    }
    
    private Map<String, Object> calcularBeneficiosSaberTT(Double puntaje) {
        Map<String, Object> beneficios = new HashMap<>();
        
        if (puntaje >= 171) {
            beneficios.put("nivel", "4");
            beneficios.put("beneficios", "Exoneración con nota 5.0 + 100% beca derechos de grado");
            beneficios.put("exoneracion_nota", "5.0");
            beneficios.put("beca_porcentaje", "100");
            beneficios.put("alerta_graduacion", false);
        } else if (puntaje >= 151) {
            beneficios.put("nivel", "3");
            beneficios.put("beneficios", "Exoneración con nota 4.7 + 50% beca derechos de grado");
            beneficios.put("exoneracion_nota", "4.7");
            beneficios.put("beca_porcentaje", "50");
            beneficios.put("alerta_graduacion", false);
        } else if (puntaje >= 120) {
            beneficios.put("nivel", "2");
            beneficios.put("beneficios", "Exoneración con nota 4.5");
            beneficios.put("exoneracion_nota", "4.5");
            beneficios.put("beca_porcentaje", "0");
            beneficios.put("alerta_graduacion", false);
        } else if (puntaje < 80) {
            beneficios.put("nivel", "1");
            beneficios.put("beneficios", "ALERTA: Riesgo de no graduación");
            beneficios.put("exoneracion_nota", "0");
            beneficios.put("beca_porcentaje", "0");
            beneficios.put("alerta_graduacion", true);
        } else {
            beneficios.put("nivel", "1");
            beneficios.put("beneficios", "Sin beneficios aplicables");
            beneficios.put("exoneracion_nota", "0");
            beneficios.put("beca_porcentaje", "0");
            beneficios.put("alerta_graduacion", false);
        }
        
        return beneficios;
    }
    
    private Map<String, Object> crearResultadoError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("nivel", "1");
        error.put("beneficios", mensaje);
        error.put("exoneracion_nota", "0");
        error.put("beca_porcentaje", "0");
        error.put("alerta_graduacion", false);
        return error;
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