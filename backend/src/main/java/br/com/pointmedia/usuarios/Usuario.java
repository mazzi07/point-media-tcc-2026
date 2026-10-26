package br.com.pointmedia.usuarios;

public record Usuario(long id, String nome, String email, String senhaHash, String perfil, boolean ativo) {
}
