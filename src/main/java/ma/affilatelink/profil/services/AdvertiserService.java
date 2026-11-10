package ma.affilatelink.profil.services;

import ma.affilatelink.profil.dto.AdvertiserRequest;
import ma.affilatelink.profil.dto.AdvertiserResponse;

import java.util.List;

public interface AdvertiserService {

    AdvertiserResponse create(AdvertiserRequest request);

    List<AdvertiserResponse> findAll();

    AdvertiserResponse findById(Long id);

}
