
package ma.affilatelink.profil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PublisherRepositoryTest {

    @Autowired
    private PublisherRepository publisherRepository;

    @Test
    void shouldPersistAndRetrievePublisher() {
        Publisher publisher = new Publisher(
                "Mohamed",
                "El Masghati",
                Niche.values()[0],
                "Java developer",
                "Morocco",
                "publisher-test@example.com"
        );

        Publisher savedPublisher = publisherRepository.saveAndFlush(publisher);

        assertNotNull(savedPublisher.getId());

        Optional<Publisher> result =
                publisherRepository.findById(savedPublisher.getId());

        assertTrue(result.isPresent());
        assertEquals("Mohamed", result.get().getFirstname());
        assertEquals("El Masghati", result.get().getLastname());
        assertEquals("publisher-test@example.com", result.get().getEmail());
    }
}
