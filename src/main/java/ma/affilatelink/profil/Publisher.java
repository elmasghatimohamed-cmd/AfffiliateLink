package ma.affilatelink.profil;

import jakarta.persistence.*;

@Entity
@Table(name = "publisher")
class Publisher extends Profile {

    @Column(nullable = false)
    private String firstname;
    @Column(nullable = false)
    private String lastname;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Niche niche;
    @Column(nullable = false)
    private String bio;
    @Column(nullable = false)
    private String country;

    protected Publisher() {}

    Publisher(String firstname, String lastname, Niche niche, String bio, String country, String email) {
        super(email);
        this.firstname = firstname;
        this.lastname = lastname;
        this.niche = niche;
        this.bio = bio;
        this.country = country;
    }

    String getFirstname() {
        return firstname;
    }

    String getLastname() {
        return lastname;
    }

    Niche getNiche() {
        return niche;
    }

    String getBio() {
        return bio;
    }

    String getCountry() {
        return country;
    }
}
