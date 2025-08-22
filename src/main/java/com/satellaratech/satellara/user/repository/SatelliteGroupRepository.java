package com.satellaratech.satellara.user.repository;

import com.satellaratech.satellara.user.model.SatelliteGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SatelliteGroupRepository extends JpaRepository<SatelliteGroup, Long> {
    Optional<SatelliteGroup> findByUser_UserIdAndGroupName(String userId, String groupName);
}
