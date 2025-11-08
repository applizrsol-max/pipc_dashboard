package com.pipc.dashboard.accounts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<AccountsEntity, Long> {
	Optional<AccountsEntity> findByCategoryNameAndProjectYear(String categoryName, String projectYear);

	Optional<AccountsEntity> findByCategoryNameAndProjectYearAndRowId(String categoryName, String projectYear,
			int rowId);

	Page<AccountsEntity> findAll(Pageable pageable);

	List<AccountsEntity> findByProjectYear(String projectYear);

	Optional<AccountsEntity> findByCategoryNameAndProjectYearAndRecordType(String categoryName, String projectYear,
			String recordType);

	Optional<AccountsEntity> findByCategoryNameAndProjectYearAndDeleteId(String category, String accountsYear,
			Long deleteId);

	Page<AccountsEntity> findByProjectYear(String year, PageRequest of);

}
