package com.ratibu.client.infrastructure;

import com.ratibu.client.domain.Client;
import com.ratibu.client.domain.ClientRepository;
import com.ratibu.client.domain.PhoneNumber;
import com.ratibu.common.domain.StableId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ClientRepositoryAdapter implements ClientRepository {

    private final SpringDataClientRepository springData;

    public ClientRepositoryAdapter(SpringDataClientRepository springData) {
        this.springData = springData;
    }

    @Override
    public Client save(Client client) {
        Optional<ClientJpaEntity> existing = springData.findById(client.getId().asUuid());
        ClientJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            ClientMapper.updateEntity(entity, client);
        } else {
            entity = ClientMapper.toEntity(client);
        }
        return ClientMapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findById(StableId id) {
        return springData.findById(id.asUuid()).map(ClientMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> findByBusinessId(StableId businessId) {
        return springData.findByBusinessId(businessId.asUuid()).stream()
                .map(ClientMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> findByBusinessIdAndPhone(StableId businessId, PhoneNumber phone) {
        return springData.findByBusinessIdAndPhone(businessId.asUuid(), phone.asString()).stream()
                .map(ClientMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(StableId id) {
        return springData.existsById(id.asUuid());
    }
}
