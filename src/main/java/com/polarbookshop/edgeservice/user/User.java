package com.polarbookshop.edgeservice.user;

import java.util.List;

/**para coletar o nome de usuário, nome, sobrenome e funções de um usuário autenticado. Essa é a mesma informação que
 fornecemos ao cadastrar os dois usuários no Keycloak, e é a informação retornada em um novo token de identificação
 **/
public record User(

        String username,
        String firstName,
        String lastName,
        List<String> roles
) {
}
