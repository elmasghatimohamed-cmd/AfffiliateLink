package ma.affilatelink.profil;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "advertiser")
class Advertiser extends Profile {

    @Column(name = "company_name", nullable = false)
    private String companyName;
    @Column(name = "sector", nullable = false)
    private String sector;
    @Column(name = "website", nullable = false)
    private String website;


    protected Advertiser(String companyName, String sector, String website, String email) {
        super(email);
        this.companyName = companyName;
        this.sector = sector;
        this.website = website;
    }

}
