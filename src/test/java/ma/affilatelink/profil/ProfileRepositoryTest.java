
package ma.affilatelink.profil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Test
    void shouldFindPublisherAndAdvertiserThroughProfileRepository() {
        Publisher publisher = new Publisher(
                "Mohamed",
                "El Masghati",
                Niche.values()[0],
                "Java developer",
                "Morocco",
                "poly-publisher@example.com"
        );

        Advertiser advertiser = new Advertiser(
                "Tech Company",
                "Technology",
                "https://example.com",
                "poly-advertiser@example.com"
        );

        publisherRepository.save(publisher);
        advertiserRepository.save(advertiser);

        List<Profile> profiles = profileRepository.findAll();

        assertEquals(2, profiles.size());

        assertTrue(profiles.stream()
                .anyMatch(profile -> profile instanceof Publisher));

        assertTrue(profiles.stream()
                .anyMatch(profile -> profile instanceof Advertiser));
    }
}
