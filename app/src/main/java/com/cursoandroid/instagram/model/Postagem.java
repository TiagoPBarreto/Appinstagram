package com.cursoandroid.instagram.model;

import com.cursoandroid.instagram.helper.ConfiguracaoFirebase;
import com.cursoandroid.instagram.helper.UsuarioFirebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Postagem {

    private String id;
    private String idUsuario;
    private String descricao;
    private String caminhoFoto;

    public Postagem() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference postagemRef = firebaseRef.child("postagens");
        String idPostagem = postagemRef.getKey();
        setId(idPostagem);
    }
    public boolean salvar(DataSnapshot seguidoresSnapshot){
        Map objeto = new HashMap();

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        //Referencia para postagem
        //firebaseRef.child("postagens").child(getIdUsuario());
        String combinacaoId = "/" + getIdUsuario() + "/" + getId();
        objeto.put("/postagens"+ combinacaoId, this);

        //Referencia para postagem
        for(DataSnapshot seguidores: seguidoresSnapshot.getChildren()) {
            String idSeguidor = seguidores.getKey();
            //Mointa objeto para salvar
            HashMap<String, Object> dadosSeguidor = new HashMap<>();
            dadosSeguidor.put("fotoPostagem", getCaminhoFoto());
            dadosSeguidor.put("descricao", getDescricao());
            dadosSeguidor.put("id", getId());
            dadosSeguidor.put("nomeUsuario", usuarioLogado.getNome());
            dadosSeguidor.put("fotoUsuario", usuarioLogado.getCaminhoFoto());
            String idsAtualizacao = "/" + idSeguidor+ "/" + getId();
            objeto.put("/feed" + idsAtualizacao, dadosSeguidor);
        }
        firebaseRef.updateChildren(objeto);
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}
