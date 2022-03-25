package com.cursoandroid.instagram.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cursoandroid.instagram.R;
import com.cursoandroid.instagram.activity.EditarPerfilActivity;
import com.cursoandroid.instagram.activity.PerfilAmigoActivity;
import com.cursoandroid.instagram.adapter.AdapterGrid;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PerfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PerfilFragment extends Fragment {
    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    public GridView gridViewPerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private Button buttonAcaoPerfil;
    private Usuario usuarioLogado;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private ValueEventListener valueEventListenerPerfil;
    private DatabaseReference firebaseRef;
    private DatabaseReference postagensUsuarioRef;
    private AdapterGrid adapterGrid;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PerfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PerfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PerfilFragment newInstance(String param1, String param2) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
   View view = inflater.inflate(R.layout.fragment_perfil, container, false);
            //Configurações iniciais

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");

        //Configura referencia postagens do usuario
        postagensUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("postagens")
                .child(usuarioLogado.getId());

            //inicializar componentes
            inicializarComponentes(view);

        //Recupera foto do usuario

        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if (caminhoFoto != null) {
            Uri url = Uri.parse(caminhoFoto);
            Glide.with(getActivity())
                    .load(url)
                    .into(imagePerfil);
        }


          //Recupera usuario logado


        // Abre edição de perfil
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), EditarPerfilActivity.class);
                startActivity(i);
            }
        });
        //Inicializar image loader
        inicialiarImageLoader();

        return view;
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

                adapterGrid = new AdapterGrid(getActivity(),R.layout.grid_postagem,urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void inicialiarImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
                .memoryCache( new LruMemoryCache( 2  *  1024  *  1024 ))
                .memoryCacheSize( 2  *  1024  *  1024 )
                .diskCacheSize( 50  *  1024  *  1024 )
                .diskCacheFileCount( 100 )
                .diskCacheFileNameGenerator( new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);
    }
    private void inicializarComponentes(View view){
        //configurações dos componentes
        gridViewPerfil = view.findViewById(R.id.gridViewPerfil);
        progressBar = view.findViewById(R.id.progressBarPerfil);
        imagePerfil = view.findViewById(R.id.imagePerfil);
        textPublicacoes = view.findViewById(R.id.textPublicacoes);
        textSeguidores = view.findViewById(R.id.textSeguidores);
        textSeguindo = view.findViewById(R.id.textSeguindo);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);

    }
    private void recuperarDadosUsuarioLogado(){
        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId());
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Usuario usuario = snapshot.getValue(Usuario.class);

               // String postagens = String.valueOf(usuario.getPostagens());
                String seguidores = String.valueOf(usuario.getSeguidores());
                String seguindo = String.valueOf(usuario.getSeguindo());

                //Configura valores recuperados
               // textPublicacoes.setText(postagens);
                textSeguidores.setText(seguidores);
                textSeguindo.setText(seguindo);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //Recupera dados do amigo selecionado
        recuperarDadosUsuarioLogado();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioLogadoRef.removeEventListener(valueEventListenerPerfil);
    }
}