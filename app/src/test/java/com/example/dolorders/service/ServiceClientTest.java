package com.example.dolorders.service;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.objet.Client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class ServiceClientTest {

    private Context context;
    private GestionnaireStockageClient storage;
    private ServiceClient service;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        storage = new GestionnaireStockageClient(context);
        storage.clearClients();

        storage.saveClients(Arrays.asList(
                client("c1", "DUPONT", "1 rue de Test", "75000", "Paris", "0102030405"),
                client("c2", "MARTIN", "10 avenue République", "69000", "Lyon", "0607080910"),
                client("c3", "DURAND", "5 place Bellecour", "69002", "Lyon", "0611111111")
        ));

        service = new ServiceClient(context);
    }

    @Test
    public void filter_criteresVides_retourneTous() {
        List<Client> res = service.filter("", "", "", "", "");
        assertThat(res).hasSize(3);
    }

    @Test
    public void filter_nullPartout_retourneTous() {
        List<Client> res = service.filter(null, null, null, null, null);
        assertThat(res).hasSize(3);
    }

    @Test
    public void filter_parNom_partiel_caseInsensitive() {
        List<Client> res = service.filter("mart", "", "", "", "");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo("c2");
    }

    @Test
    public void filter_parVille_retourneDeux() {
        List<Client> res = service.filter("", "", "", "lyon", "");
        assertThat(res).hasSize(2);
        assertThat(res.get(0).getVille().toLowerCase()).contains("lyon");
        assertThat(res.get(1).getVille().toLowerCase()).contains("lyon");
    }

    @Test
    public void filter_multiCriteres_villeLyon_et_cp69000_retourneUn() {
        List<Client> res = service.filter("", "", "69000", "lyon", "");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo("c2");
    }

    @Test
    public void filter_parTelephone_partiel_retourneUn() {
        List<Client> res = service.filter("", "", "", "", "0611");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo("c3");
    }

    @Test
    public void filter_aucunMatch_retourneVide() {
        List<Client> res = service.filter("___NO_MATCH___", "", "", "", "");
        assertThat(res).isEmpty();
    }

    @Test
    public void filter_parAdresse_partiel_retourneUn() {
        List<Client> res = service.filter("", "bellecour", "", "", "");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo("c3");
    }

    @Test
    public void filter_parCodePostal_exact_retourneUn() {
        List<Client> res = service.filter("", "", "75000", "", "");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getCodePostal()).isEqualTo("75000");
    }

    private Client client(String id, String nom, String adresse, String cp, String ville, String tel) {
        return new Client.Builder()
                .setId(id)
                .setNom(nom)
                .setAdresse(adresse)
                .setCodePostal(cp)
                .setVille(ville)
                .setTelephone(tel)
                .setAdresseMail(nom.toLowerCase() + "@test.fr")
                .setUtilisateur("unit-test")
                .setDateSaisie(new GregorianCalendar(2026, Calendar.JANUARY, 23).getTime())
                .build();
    }
}
