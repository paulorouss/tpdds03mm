package domain.organizaciones.contacto;

import lombok.Getter;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "contacto")
public class Contacto {
    @Id
    @GeneratedValue
    private int id;

    @Getter
    @Column(name = "telefono",nullable = false)
    private String nroTelefono;

    @Getter
    @Column(name = "email",nullable = false)
    private String email;

    @Transient //se persiste con el enum
    public List<MedioNotificacion> accionesNotificar;

    @ElementCollection
    @CollectionTable(name = "medios_notificacion",joinColumns = @JoinColumn(name = "contacto_id",referencedColumnName = "id"))
    @Enumerated(value = EnumType.STRING)
    private List<EMedioNotificacion> mediosDeNotificacion;

    public Contacto() {
    }

    public Contacto(String nroTelefono, String mail, MedioNotificacion... acciones) {
        this.nroTelefono = nroTelefono;
        this.email = mail;
        accionesNotificar = new ArrayList<>();
        accionesNotificar.addAll(Arrays.asList(acciones));
    }

    public void notificar(String contenido){ //todo factory con el enum
        accionesNotificar.forEach(t-> {
            try {
                t.notificar(contenido,this.getNroTelefono(),this.getEmail());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
