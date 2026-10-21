package ma.affilatelink.profil;

import jakarta.persistence.*;

@Entity
@Table(name = "advertiser")
class Advertiser extends Profile {

    @Column(nullable = false)
    private String companyName;
    @Column(nullable = false)
    private String sector;
    @Column(nullable = false)
    private String website;

    protected Advertiser() {}

    protected Advertiser(String companyName, String sector, String website, String email) {
        super(email);
        this.companyName = companyName;
        this.sector = sector;
        this.website = website;
    }

    String getCompanyName() {
        return companyName;
    }

    String getSector() {
        return sector;
    }

    String getWebsite() {
        return website;
    }
}
