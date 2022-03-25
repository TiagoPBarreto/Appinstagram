package com.cursoandroid.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cursoandroid.instagram.R;
import com.cursoandroid.instagram.adapter.AdapterGrid;
import com.cursoandroid.instagram.fragment.PostagemFragment;
import com.cursoandroid.instagram.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagram.helper.UsuarioFirebase;
import com.cursoandroid.instagram.model.Postagem;
import com.cursoandroid.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {
    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private GridView gridViewPerfil;
    private AdapterGrid adapterGrid;

    private DatabaseReference firebaseRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference postagensUsuarioRef;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private DatabaseReference seguidoresRef;
    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //Configuração iniciais

        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        //Iniciar Componentes

        inicializarComponentes();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Perfilt");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24);

        //Recuperar usuario selecionado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioSelecionado");

            //Configura referencia postagens do usuario
            postagensUsuarioRef = ConfiguracaoFirebase.getFirebase()
                    .child("postagens")
                    .child(usuarioSelecionado.getId());

            //Configura o nome do Usuario na toolbar

            getSupportActionBar().setTitle(usuarioSelecionado.getNome());

            //Recupera foto do usuario

            String caminhoFoto = usuarioSelecionado.getCaminhoFoto();
            if (caminhoFoto != null) {
                Uri url = Uri.parse(caminhoFoto);
                Glide.with(PerfilAmigoActivity.this)
                        .load(url)
                        .into(imagePerfil);
            }

        }
        //Inicializar image loader
        inicialiarImageLoader();
        //Carregar as fotos das postagens de um usuario
        carregarFotosPostagem();
        //Abre a foto clicada

    }

    public void inicialiarImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCache( new LruMemoryCache( 2  *  1024  *  1024 ))
                .memoryCacheSize( 2  *  1024  *  1024 )
                .diskCacheSize( 50  *  1024  *  1024 )
                .diskCacheFileCount( 100 )
                .diskCacheFileNameGenerator( new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);
    }
    public void carregarFotosPostagem(){
    // Recupera as fotos postadas pelo usuario
        postagensUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Configura o tamanho do grid

                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                int tamanhoImagem = tamanhoGrid / 3;
                gridViewPerfil.setColumnWidth(tamanhoImagem);
                //Recuperar usuario selecionado
                List<String> urlFotos = new ArrayList<>();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    urlFotos.add(postagem.getCaminhoFoto());
                }
                int qtdPostagens = urlFotos.size();
                textPublicacoes.setText(String.valueOf(qtdPostagens));

                //Configurar adapter

                adapterGrid = new AdapterGrid(getApplicationContext(),R.layout.grid_postagem,urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void recuperaDadosUsuarioLogado(){
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Recupera dados do usuario logado
                usuarioLogado = snapshot.getValue(Usuario.class);

                //Verificar se usuario ja esta seguindoamigo selecionado
                verificaSegueUsuarioAmigo();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void verificaSegueUsuarioAmigo(){

        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child(idUsuarioLogado);
        seguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //ja seguindo
                    habilitarBotaoSeguir(true);
                }else{
                    //Ainda não esta seguindo
                    habilitarBotaoSeguir(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void habilitarBotaoSeguir(boolean segueUsuario){

        if(segueUsuario){
            buttonAcaoPerfil.setText("Seguindo");
        }else{
            buttonAcaoPerfil.setText("Seguir");
            //Adiciona evento para seguir usuario

            buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Salvar seguidor
                    salvarSeguidor(usuarioLogado,usuarioSelecionado);
                }
            });
        }

    }
    private void salvarSeguidor(Usuario uLogado,Usuario uAmigo){
        HashMap<String,Object> dadosUsuarioLogado = new HashMap<>();
        dadosUsuarioLogado.put("nome",uAmigo.getNome());
        dadosUsuarioLogado.put("caminhoFoto",uLogado.getCaminhoFoto());
        DatabaseReference seguidorRef = seguidoresRef.child(uAmigo.getId())
                .child(uLogado.getId());
        seguidorRef.setValue(dadosUsuarioLogado);
        //Alterar botão acao para seguindo
        buttonAcaoPerfil.setText("Seguindo");
        buttonAcaoPerfil.setOnClickListener(null);

        //Incrementar seguindo do usuario logado

        int seguindo = uLogado.getSeguindo() + 1;
        HashMap<String,Object> dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo",seguindo);
        DatabaseReference usuarioSeguindo = usuariosRef
                .child(uLogado.getId());
        usuarioSeguindo.updateChildren(dadosSeguindo);

        // Incrementar seguidores do amigo

        int seguidores = uAmigo.getSeguidores() + 1;
        HashMap<String,Object> dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores",seguidores);
        DatabaseReference usuarioSeguidores = usuariosRef
                .child(uAmigo.getId());
        usuarioSeguidores.updateChildren(dadosSeguindo);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Recupera dados do amigo selecionado
        recuperarDadosPerfilAmigo();
        //Recuperar dados usuario logado
        recuperaDadosUsuarioLogado();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }

    private void recuperarDadosPerfilAmigo(){
        usuarioAmigoRef = usuariosRef.child(usuarioSelecionado.getId());
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Usuario usuario = snapshot.getValue(Usuario.class);

                String postagens = String.valueOf(usuario.getPostagens());
                String seguidores = String.valueOf(usuario.getSeguidores());
                String seguindo = String.valueOf(usuario.getSeguindo());

                //Configura valores recuperados
                textPublicacoes.setText(postagens);
                textSeguidores.setText(seguidores);
                textSeguindo.setText(seguindo);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void inicializarComponentes(){
        imagePerfil = findViewById(R.id.imagePerfil);
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        buttonAcaoPerfil.setText("Carregando");
        textPublicacoes = findViewById(R.id.textPublicacoes);
        textSeguidores = findViewById(R.id.textSeguidores);
        textSeguindo = findViewById(R.id.textSeguindo);
        gridViewPerfil = findViewById(R.id.gridViewPerfil);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}