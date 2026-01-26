package com.example.dolorders.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ClientsAjoutFragmentViewModel extends ViewModel {

    // On utilise MutableLiveData pour pouvoir modifier les données depuis le ViewModel
    private final MutableLiveData<String> nom = new MutableLiveData<>("");
    private final MutableLiveData<String> adresse = new MutableLiveData<>("");
    private final MutableLiveData<String> codePostal = new MutableLiveData<>("");
    private final MutableLiveData<String> ville = new MutableLiveData<>("");
    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> telephone = new MutableLiveData<>("");

    // On expose des LiveData immuables au fragment pour qu'il ne puisse que les observer
    public LiveData<String> getNom() {
        return nom;
    }

    public LiveData<String> getAdresse() {
        return adresse;
    }

    public LiveData<String> getCodePostal() {
        return codePostal;
    }

    public LiveData<String> getVille() {
        return ville;
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<String> getTelephone() {
        return telephone;
    }

    // Méthodes pour mettre à jour les données depuis le fragment
    public void setNom(String value) {
        nom.setValue(value);
    }

    public void setAdresse(String value) {
        adresse.setValue(value);
    }

    public void setCodePostal(String value) {
        codePostal.setValue(value);
    }

    public void setVille(String value) {
        ville.setValue(value);
    }

    public void setEmail(String value) {
        email.setValue(value);
    }

    public void setTelephone(String value) {
        telephone.setValue(value);
    }

    // Méthode pour vider le formulaire
    public void clear() {
        nom.setValue("");
        adresse.setValue("");
        codePostal.setValue("");
        ville.setValue("");
        email.setValue("");
        telephone.setValue("");
    }
}

