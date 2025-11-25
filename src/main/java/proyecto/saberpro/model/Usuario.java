package proyecto.saberpro.model;

import jakarta.persistence.*;
// import lombok.*; // Comenta Lombok temporalmente

@Entity
@Table(name = "usuarios")
// @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String documento;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol; // COORDINADOR o ESTUDIANTE

    // Constructor vacío
    public Usuario() {}

    // Constructor con todos los campos
    public Usuario(Long id, String documento, String password, String rol) {
        this.id = id;
        this.documento = documento;
        this.password = password;
        this.rol = rol;
    }

    // GETTERS Y SETTERS MANUALES
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}