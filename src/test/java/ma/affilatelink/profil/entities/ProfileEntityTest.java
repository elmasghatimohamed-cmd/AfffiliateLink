
package ma.affilatelink.profil.entities;

import ma.affilatelink.common.exceptions.InvalidProfileStatusException;
import ma.affilatelink.profil.enums.Niche;
import ma.affilatelink.profil.enums.ProfileStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileEntityTest {

    @Test
    void publisherShouldStartWithPendingStatus() {
        Publisher publisher = new Publisher(
                "Mohamed",
                "El Masghati",
                Niche.BEAUTY,
                "Content creator",
                "Morocco",
                "publisher@test.com"
        );

        assertEquals(ProfileStatus.PENDING, publisher.getStatus());
        assertEquals("publisher@test.com", publisher.getEmail());
        assertNotNull(publisher.getCreatedAt());
    }

    @Test
    void advertiserShouldStartWithPendingStatus() {
        Advertiser advertiser = new Advertiser(
                "AffiliateLink",
                "Technology",
                "https://example.com",
                "advertiser@test.com"
        );

        assertEquals(ProfileStatus.PENDING, advertiser.getStatus());
        assertEquals("advertiser@test.com", advertiser.getEmail());
        assertNotNull(advertiser.getCreatedAt());
    }

    @Test
    void pendingProfileShouldBeActivated() {
        Publisher publisher = createPublisher();

        publisher.activate();

        assertEquals(ProfileStatus.ACTIVE, publisher.getStatus());
    }

    @Test
    void activeProfileShouldBeSuspended() {
        Publisher publisher = createPublisher();
        publisher.activate();

        publisher.suspend();

        assertEquals(ProfileStatus.SUSPENDED, publisher.getStatus());
    }

    @Test
    void suspendedProfileShouldBeReactivated() {
        Publisher publisher = createPublisher();
        publisher.activate();
        publisher.suspend();

        publisher.activate();

        assertEquals(ProfileStatus.ACTIVE, publisher.getStatus());
    }

    @Test
    void activeProfileShouldNotBeActivatedAgain() {
        Publisher publisher = createPublisher();
        publisher.activate();

        assertThrows(
                InvalidProfileStatusException.class,
                publisher::activate
        );
    }

    @Test
    void pendingProfileShouldNotBeSuspended() {
        Publisher publisher = createPublisher();

        assertThrows(
                InvalidProfileStatusException.class,
                publisher::suspend
        );
    }

    @Test
    void suspendedProfileShouldNotBeSuspendedAgain() {
        Publisher publisher = createPublisher();
        publisher.activate();
        publisher.suspend();

        assertThrows(
                InvalidProfileStatusException.class,
                publisher::suspend
        );
    }

    private Publisher createPublisher() {
        return new Publisher(
                "Mohamed",
                "El Masghati",
                Niche.BEAUTY,
                "Content creator",
                "Morocco",
                "publisher@test.com"
        );
    }
}
