package com.pipc.dashboard.accounts.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<AccountsEntity, Long> {
	Optional<AccountsEntity> findByCategoryNameAndProjectYear(String categoryName, String projectYear);

	Optional<AccountsEntity> findByCategoryNameAndProjectYearAndRowId(String categoryName, String projectYear,
			int rowId);

	Page<AccountsEntity> findAll(Pageable pageable);
}
