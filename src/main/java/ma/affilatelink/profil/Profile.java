package ma.affilatelink.profil;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "profile")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus status =  ProfileStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Profile() {}

    protected Profile(String email) {
        this.email = email;
    }

    Long getId() {
        return id;
    }

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    ProfileStatus getStatus() {
        return status;
    }

    LocalDateTime getCreatedAt() {
        return createdAt;
    }

    void activate() {
        this.status = ProfileStatus.ACTIVE;
    }

    void suspend() {
        this.status = ProfileStatus.SUSPENDED;
    }

}
