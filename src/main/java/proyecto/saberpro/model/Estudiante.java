package proyecto.saberpro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiantes")
public class Estudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipoDocumento;

    @Column(nullable = false, unique = true)
    private String numeroDocumento;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    private String correo;
    private String telefono;
    

    @Column(nullable = false)
    private boolean activo = true;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @Column(name = "resultados_disponibles")
    private boolean resultadosDisponibles = false;

    // Constructores
    public Estudiante() {}

    public Estudiante(String tipoDocumento, String numeroDocumento, String nombres, 
                     String apellidos, String correo, String telefono, boolean activo, Usuario usuario) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.activo = activo;
        this.usuario = usuario;
    }

    // Getters y Setters
    
    public boolean isResultadosDisponibles() { 
        return resultadosDisponibles; 
    }
    public void setResultadosDisponibles(boolean resultadosDisponibles) { 
        this.resultadosDisponibles = resultadosDisponibles; 
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}