package proyecto.saberpro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resultados")
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(nullable = false)
    private Double puntaje;

    @Column(name = "numero_registro")
    private String numeroRegistro;
    
    @Column(name = "nivel_ingles")
    private String nivelIngles; // A0, A1, A2, B1, B2

    @Column(name = "tipo_prueba", nullable = false)
    private String tipoPrueba;

    @Column(name = "puntaje_general")
    private Double puntajeGeneral;

    private String nivel;

    private String beneficios;

    // ✅ NUEVOS CAMPOS PARA BENEFICIOS
    @Column(name = "exoneracion_nota")
    private String exoneracionNota;

    @Column(name = "beca_porcentaje")
    private String becaPorcentaje;

    @Column(name = "alerta_graduacion")
    private Boolean alertaGraduacion = false;

    public Resultado() {
    }

    public Resultado(Estudiante estudiante, Competencia competencia, Double puntaje) {
        this.estudiante = estudiante;
        this.competencia = competencia;
        this.puntaje = puntaje;
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNivelIngles() { return nivelIngles; }
    public void setNivelIngles(String nivelIngles) { this.nivelIngles = nivelIngles; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public Competencia getCompetencia() { return competencia; }
    public void setCompetencia(Competencia competencia) { this.competencia = competencia; }

    public Double getPuntaje() { return puntaje; }
    public void setPuntaje(Double puntaje) { this.puntaje = puntaje; }

    public String getNumeroRegistro() { return numeroRegistro; }
    public void setNumeroRegistro(String numeroRegistro) { this.numeroRegistro = numeroRegistro; }

    public String getTipoPrueba() { return tipoPrueba; }
    public void setTipoPrueba(String tipoPrueba) { this.tipoPrueba = tipoPrueba; }

    public Double getPuntajeGeneral() { return puntajeGeneral; }
    public void setPuntajeGeneral(Double puntajeGeneral) { this.puntajeGeneral = puntajeGeneral; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getBeneficios() { return beneficios; }
    public void setBeneficios(String beneficios) { this.beneficios = beneficios; }

    // ✅ NUEVOS GETTERS Y SETTERS
    public String getExoneracionNota() { return exoneracionNota; }
    public void setExoneracionNota(String exoneracionNota) { this.exoneracionNota = exoneracionNota; }

    public String getBecaPorcentaje() { return becaPorcentaje; }
    public void setBecaPorcentaje(String becaPorcentaje) { this.becaPorcentaje = becaPorcentaje; }

    public Boolean getAlertaGraduacion() { return alertaGraduacion; }
    public void setAlertaGraduacion(Boolean alertaGraduacion) { this.alertaGraduacion = alertaGraduacion; }
}