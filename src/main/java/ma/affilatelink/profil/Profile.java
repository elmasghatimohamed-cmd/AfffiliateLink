package ma.affilatelink.profil;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "profile")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email",unique = true,nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private ProfileStatus status =  ProfileStatus.PENDING;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Profile(String email) {
        this.email = email;
    }



    void activate() {
        this.status = ProfileStatus.ACTIVE;
    }

    void suspend() {
        this.status = ProfileStatus.SUSPENDED;
    }

}
