package com.findapi.api.collection.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.CollectionApiEntity;
import com.findapi.api.entity.CollectionApiId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionApiRepository extends JpaRepository<CollectionApiEntity, CollectionApiId> {
    Optional<CollectionApiEntity> findByCollectionIdAndApiId(UUID collectionId, UUID apiId);

    Optional<CollectionApiEntity> findByCollectionIdAndApiIdAndDeletedAtIsNull(UUID collectionId, UUID apiId);

    List<CollectionApiEntity> findByCollectionIdAndDeletedAtIsNull(UUID collectionId);
}
