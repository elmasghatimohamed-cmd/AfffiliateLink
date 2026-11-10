package ma.affilatelink.profil.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "advertiser")
public class Advertiser extends Profile {

    @Column(name = "company_name", nullable = false)
    private String companyName;
    @Column(name = "sector", nullable = false)
    private String sector;
    @Column(name = "website", nullable = false)
    private String website;


    public Advertiser(String companyName, String sector, String website, String email) {
        super(email);
        this.companyName = companyName;
        this.sector = sector;
        this.website = website;
    }

}
