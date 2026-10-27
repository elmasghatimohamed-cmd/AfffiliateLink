package ma.affilatelink.profil.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ma.affilatelink.profil.enums.Niche;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "publisher")
public class Publisher extends Profile {

    @Column(name  = "firstname", nullable = false)
    private String firstname;
    @Column(name = "lastname", nullable = false)
    private String lastname;
    @Enumerated(EnumType.STRING)
    @Column(name = "niche", nullable = false)
    private Niche niche;
    @Column(name = "bio", nullable = false)
    private String bio;
    @Column(name = "country", nullable = false)
    private String country;


    public Publisher(String firstname, String lastname, Niche niche, String bio, String country, String email) {
        super(email);
        this.firstname = firstname;
        this.lastname = lastname;
        this.niche = niche;
        this.bio = bio;
        this.country = country;
    }
}
