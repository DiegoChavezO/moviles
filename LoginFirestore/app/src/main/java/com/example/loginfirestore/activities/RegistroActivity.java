package com.example.loginfirestore.activities;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.loginfirestore.R;
import com.example.loginfirestore.databinding.ActivityLoginBinding;
import com.example.loginfirestore.utilities.Constants;
import com.example.loginfirestore.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners(){
        binding.txtCuentaNueva.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),RegistroActivity.class)));
        binding.btnIniciarSesion.setOnClickListener(v -> {
            if(ValidarLogin()){
                IniciarSesion();
            }
        });
    }
    private void IniciarSesion(){
        Cargando(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.etCorreo.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.etContrasena.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,
                                documentSnapshot.getString(Constants.KEY_NAME));

                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        Cargando(false);
                        muestraToast("No es posible iniciar sesión");
                    }
                });
    }
    private void Cargando(Boolean isLoading){
        if(isLoading){
            binding.btnIniciarSesion.setVisibility(View.INVISIBLE);
            binding.BarraProgreso.setVisibility(View.VISIBLE);
        }else{
            binding.BarraProgreso.setVisibility(View.INVISIBLE);
            binding.btnIniciarSesion.setVisibility(View.VISIBLE);
        }
    }
    private void muestraToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private Boolean ValidarLogin(){
        if(binding.etCorreo.getText().toString().trim().isEmpty()){
            muestraToast("Ingresa tu correo electrónico");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.etCorreo.getText().toString()).matches()) {
            muestraToast("Ingresa un correo electrónico válido");
            return false;
        }else if(binding.etContrasena.getText().toString().trim().isEmpty()){
            muestraToast("Ingresa una contraseña");
            return false;
        }else{
            return true;
        }
    }
}
