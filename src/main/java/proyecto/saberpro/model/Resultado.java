package proyecto.saberpro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resultados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    // NUEVOS CAMPOS PARA SPRINT 2
    @Column(name = "numero_registro")
    private String numeroRegistro;

    @Column(name = "tipo_prueba", nullable = false)
    private String tipoPrueba; // "SABER_PRO" o "SABER_TT"

    @Column(name = "puntaje_general")
    private Double puntajeGeneral; // Puntaje global (0-300 o 0-200)

    private String nivel; // "1", "2", "3", "4"

    private String beneficios; // Descripción de beneficios
    
    public Resultado() {
    }

    // Constructor sin los nuevos campos (para compatibilidad)
    public Resultado(Estudiante estudiante, Competencia competencia, Double puntaje) {
        this.estudiante = estudiante;
        this.competencia = competencia;
        this.puntaje = puntaje;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Estudiante getEstudiante() {
		return estudiante;
	}

	public void setEstudiante(Estudiante estudiante) {
		this.estudiante = estudiante;
	}

	public Competencia getCompetencia() {
		return competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	public Double getPuntaje() {
		return puntaje;
	}

	public void setPuntaje(Double puntaje) {
		this.puntaje = puntaje;
	}

	public String getNumeroRegistro() {
		return numeroRegistro;
	}

	public void setNumeroRegistro(String numeroRegistro) {
		this.numeroRegistro = numeroRegistro;
	}

	public String getTipoPrueba() {
		return tipoPrueba;
	}

	public void setTipoPrueba(String tipoPrueba) {
		this.tipoPrueba = tipoPrueba;
	}

	public Double getPuntajeGeneral() {
		return puntajeGeneral;
	}

	public void setPuntajeGeneral(Double puntajeGeneral) {
		this.puntajeGeneral = puntajeGeneral;
	}

	public String getNivel() {
		return nivel;
	}

	public void setNivel(String nivel) {
		this.nivel = nivel;
	}

	public String getBeneficios() {
		return beneficios;
	}

	public void setBeneficios(String beneficios) {
		this.beneficios = beneficios;
	}
    
}