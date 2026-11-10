package ma.affilatelink.profil.services.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.affilatelink.common.exceptions.EmailAlreadyExistsException;
import ma.affilatelink.profil.dto.PublisherRequest;
import ma.affilatelink.profil.dto.PublisherResponse;
import ma.affilatelink.profil.entities.Publisher;
import ma.affilatelink.profil.repositories.ProfileRepository;
import ma.affilatelink.profil.repositories.PublisherRepository;
import ma.affilatelink.profil.services.PublisherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public PublisherResponse create(PublisherRequest request) {
        log.info("Creating publisher profile");

        if (profileRepository.existsByEmail(request.email())) {
            log.warn("Publisher creation rejected: email already exists");

            throw new EmailAlreadyExistsException(request.email());
        }

        Publisher publisher = new Publisher(
                request.firstname(),
                request.lastname(),
                request.niche(),
                request.bio(),
                request.country(),
                request.email()
        );

        publisher = publisherRepository.save(publisher);

        log.info(
                "Publisher profile created successfully with ID: {}",
                publisher.getId()
        );

        return toResponse(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublisherResponse> findAll() {
        log.debug("Retrieving all publisher profiles");

        List<PublisherResponse> publishers = publisherRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        log.debug("Retrieved {} publisher profiles", publishers.size());

        return publishers;
    }

    @Override
    @Transactional(readOnly = true)
    public PublisherResponse findById(Long id) {
        log.debug("Retrieving publisher profile with ID: {}", id);

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Publisher not found with id: " + id
                        )
                );

        return toResponse(publisher);
    }

    private PublisherResponse toResponse(Publisher publisher) {
        return new PublisherResponse(
                publisher.getId(),
                publisher.getEmail(),
                publisher.getStatus(),
                publisher.getCreatedAt(),
                publisher.getFirstname(),
                publisher.getLastname(),
                publisher.getNiche(),
                publisher.getBio(),
                publisher.getCountry()
        );
    }
}