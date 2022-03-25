package com.cursoandroid.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cursoandroid.instagram.R;
import com.cursoandroid.instagram.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagram.helper.UsuarioFirebase;
import com.cursoandroid.instagram.model.Postagem;
import com.cursoandroid.instagram.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.logging.Filter;

public class FiltroActivity extends AppCompatActivity {
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private ImageView imageFotoEscolhida;
    private Bitmap imagem;
    private Bitmap imagemFiltro;
    private String idUsuarioLogado;
    private TextInputEditText textDescricaoFiltro;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    private Usuario usuarioLogado;
    private AlertDialog dialog;
    private DataSnapshot seguidoresSnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        //Configurações iniciais
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        usuariosRef = ConfiguracaoFirebase.getFirebase().child("usuarios");
        firebaseRef = ConfiguracaoFirebase.getFirebase();

        //Inicializa componentes

        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);
        textDescricaoFiltro = findViewById(R.id.textDescricaoFiltro);


        //Recuperar dados para uma nova postagem
        recuperaDadosPostagem();

        //Configuração toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Filtros");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24);

        //recupera a imagem escolhida pelo usuario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            byte[] dadosImagem = bundle.getByteArray("fotoEscolhida");
            imagem = BitmapFactory.decodeByteArray(dadosImagem, 0, dadosImagem.length);
            imageFotoEscolhida.setImageBitmap(imagem);

            //Recupera filtros
            recuperarFiltro();

            //imagemFiltro = imagem.copy(imagem.getConfig(),true);


        }

    }

    private void recuperarFiltro() {


}
private void abrirDialogCarregamento(String titulo){
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle(titulo);
    alert.setCancelable(false);
    alert.setView(R.layout.carregando);
    dialog = alert.create();
    dialog.show();



    }



    private void recuperaDadosPostagem(){

        abrirDialogCarregamento("Carregando dados, aguarde!");
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Recupera dados do usuario logado
                usuarioLogado = snapshot.getValue(Usuario.class);
                // Recuperar os seguidores
                DatabaseReference seguidoesRef = firebaseRef.child("seguidores")
                        .child(idUsuarioLogado);
                seguidoesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        seguidoresSnapshot = snapshot;
                        dialog.cancel();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void publicarPostagem() {

        abrirDialogCarregamento("Salvando Postagem");
        Postagem postagem = new Postagem();
        postagem.setIdUsuario(idUsuarioLogado);
        postagem.setDescricao(textDescricaoFiltro.getText().toString());

        //Recuperar dados da imagem para o firebase

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] dadosImagem = baos.toByteArray();

        //Salvar imagem no firebase storage
        StorageReference storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        final StorageReference imagemRef = storageRef
                .child("imagens")
                .child("postagens")
                .child(postagem.getId());

        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FiltroActivity.this,
                        "Erro ao salvar a imagem",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Recuperar local da foto
                imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri url = task.getResult();
                        int qtdPostagens = usuarioLogado.getPostagens() +1;
                        usuarioLogado.setPostagens(qtdPostagens);
                        usuarioLogado.atualizarQtdPostagem();
                        //Salvar postagem
                        if (postagem.salvar()) {

                            Toast.makeText(FiltroActivity.this,
                                    "Sucesso ao salvar postagem ",
                                    Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            finish();
                        }
                    }


                });
            }

            @Override
            public boolean onCreateOptionsMenu(Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_file, menu);
                return FiltroActivity.super.onCreateOptionsMenu(menu);
            }

            @Override
            public boolean onOptionsItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_salvar_postagem:
                        publicarPostagem();
                        break;
                }
                return FiltroActivity.super.onOptionsItemSelected(item);
            }

            @Override
            public boolean onSupportNavigateUp() {
                finish();
                return FiltroActivity.super.onSupportNavigateUp();
            }

        }