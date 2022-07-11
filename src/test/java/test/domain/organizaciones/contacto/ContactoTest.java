package test.domain.organizaciones.contacto;

import domain.organizaciones.Organizacion;
import domain.organizaciones.contacto.Contacto;
import domain.organizaciones.contacto.EnvioNotificacionJavaxMailAdapter;
import domain.organizaciones.contacto.EnvioNotificacionUltraWppAdapter;
import domain.organizaciones.contacto.EnvioNotificacionWhatsappAdapter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContactoTest {

    Contacto contacto;
    Organizacion organizacion;
    EnvioNotificacionJavaxMailAdapter mailAdapter;
    EnvioNotificacionUltraWppAdapter whatsappAdapter;


    @BeforeEach
    public void init() throws IOException {
//        mailAdapter = mock(EnvioNotificacionJavaxMailAdapter.class);
        whatsappAdapter = mock(EnvioNotificacionUltraWppAdapter.class);
        mailAdapter =
                new EnvioNotificacionJavaxMailAdapter("src/main/java/domain/organizaciones/contacto/smtp.properties",
                        "Guia de recomendaciones");
//        whatsappAdapter =
//                new EnvioNotificacionUltraWppAdapter("src/main/java/domain/organizaciones/contacto/token.properties",
//            "https://api.ultramsg.com/instance10585/messages/chat");
        contacto = new Contacto("12345678", "test",
                whatsappAdapter, mailAdapter);
        List<Contacto> contactos = new ArrayList<>();
        contactos.add(contacto);
        organizacion = new Organizacion();
        organizacion.setContactos(contactos);

        //mailAdapter.generarSesion();
    }

    @Test
    @DisplayName("Contacto: se manda un whatsapp")
    public void whatsapp() throws IOException {
        OkHttpClient cliente = new OkHttpClient();

        String numero = "token=" + "abcd123" + "&to=+54" + contacto.getNroTelefono() + "&body=" + "Buenos dias" + "&priority=1&referenceId=";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody cuerpo = RequestBody.create(numero,mediaType);
        Request request = new Request.Builder()
                .url("https://test.com")
                .post(cuerpo)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();
        when(whatsappAdapter.ejecutarRequest(cliente,request)).thenReturn("200");

        Assertions.assertEquals("200",whatsappAdapter.ejecutarRequest(cliente,request));
    }

    @Test
    @DisplayName("Contacto: se manda un mail")
    public void mail() throws MessagingException {
        MimeMessage mensajeTest = mailAdapter.armarMensaje(mailAdapter.generarSesion(),contacto.getEmail(),"Buenos dias");
        Assertions.assertEquals("Guia de recomendaciones",mensajeTest.getSubject());
    }
}
