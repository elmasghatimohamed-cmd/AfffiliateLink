package ma.affilatelink.profil;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


import ma.affilatelink.common.InvalidProfileStatusException;


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
        if (status != ProfileStatus.PENDING
                && status != ProfileStatus.SUSPENDED) {
            throw new InvalidProfileStatusException(
                    "Cannot activate a profile with status: " + status
            );
        }

        this.status = ProfileStatus.ACTIVE;
    }

    void suspend() {
        if (status != ProfileStatus.ACTIVE) {
            throw new InvalidProfileStatusException(
                    "Cannot suspend a profile with status: " + status
            );
        }

        this.status = ProfileStatus.SUSPENDED;
    }


}
