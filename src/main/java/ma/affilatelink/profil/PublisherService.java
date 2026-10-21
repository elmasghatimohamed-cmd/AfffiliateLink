package ma.affilatelink.profil;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.common.EmailAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public PublisherResponse create(PublisherRequest request) {

        if (profileRepository.existsByEmail(request.email())) {
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

    @Transactional(readOnly = true)
    public List<PublisherResponse> findAll() {
        return publisherRepository.findAll()
                .stream()
                .map(publisher -> new PublisherResponse(
                        publisher.getId(),
                        publisher.getEmail(),
                        publisher.getStatus(),
                        publisher.getCreatedAt(),
                        publisher.getFirstname(),
                        publisher.getLastname(),
                        publisher.getNiche(),
                        publisher.getBio(),
                        publisher.getCountry()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublisherResponse findById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Publisher not found with id: " + id)
                );

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