package ma.affilatelink.profil.services;

import ma.affilatelink.profil.enums.ProfileStatus;

public interface AdminService {

    void changeStatus(Long id, ProfileStatus newStatus);

}
