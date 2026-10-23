
package ma.affilatelink.profil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdvertiserRepositoryTest {

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Test
    void shouldPersistAndRetrieveAdvertiser() {
        Advertiser advertiser = new Advertiser(
                "Tech Company",
                "Technology",
                "https://example.com",
                "advertiser-test@example.com"
        );

        Advertiser savedAdvertiser =
                advertiserRepository.saveAndFlush(advertiser);

        assertNotNull(savedAdvertiser.getId());

        Optional<Advertiser> result =
                advertiserRepository.findById(savedAdvertiser.getId());

        assertTrue(result.isPresent());
        assertEquals("Tech Company", result.get().getCompanyName());
        assertEquals("Technology", result.get().getSector());
        assertEquals("https://example.com", result.get().getWebsite());
        assertEquals(
                "advertiser-test@example.com",
                result.get().getEmail()
        );
    }
}
