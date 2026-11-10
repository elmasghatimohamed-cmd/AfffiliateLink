package ma.affilatelink.profil.services;

import ma.affilatelink.profil.dto.PublisherRequest;
import ma.affilatelink.profil.dto.PublisherResponse;

import java.util.List;

public interface PublisherService {

    PublisherResponse create(PublisherRequest request);

    List<PublisherResponse> findAll();

    PublisherResponse findById(Long id);

}
