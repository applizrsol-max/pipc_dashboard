package com.pipc.dashboard.accounts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<AccountsEntity, Long> {
	Optional<AccountsEntity> findByCategoryNameAndProjectYear(String categoryName, String projectYear);
}
