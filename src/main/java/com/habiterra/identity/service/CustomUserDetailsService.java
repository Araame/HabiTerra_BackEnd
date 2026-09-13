package com.habiterra.identity.service;
import com.habiterra.identity.entity.*;
import com.habiterra.identity.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;


//Retrieving the connected user details infos by implementing UserDetailsService from DAOAuthenticationProvider
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UtilisateurRepository users;
    private final IdentifierService identifiers;

    public CustomUserDetailsService(UtilisateurRepository users,IdentifierService identifiers){
        this.users=users;
        this.identifiers=identifiers;
    }

//    Retrieving the users infos by Identifier
    public UserDetails loadUserByUsername(String input) {
        var id=identifiers.normalize(input);
        Utilisateur user=(id.type()==IdentifierType.EMAIL?users.findByEmail(id.value()):users.findByTelephone(id.value()))
            .orElseThrow(()->new UsernameNotFoundException("Identifiants incorrects"));
        // Status is checked after password authentication, to avoid leaking it to an unauthenticated caller.
        return User.withUsername(user.getIdUtilisateur().toString()).password(user.getMotDePasse()).roles(user.getRole().name()).build();
    }
}
