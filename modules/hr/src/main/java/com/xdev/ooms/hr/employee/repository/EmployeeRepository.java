package com.xdev.ooms.hr.employee.repository;

import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeRepository extends BaseRepository<Employee> {

    @Query("""
            SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
            FROM Employee e
            WHERE e.cin = :cin
              AND e.isDeleted = false
              AND (:excludeId IS NULL OR e.id <> :excludeId)
            """)
    boolean existsByCinAndIdNotAndIsDeletedFalse(@Param("cin") String cin, @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
            FROM Employee e
            WHERE e.cnssMatricule = :cnssMatricule
              AND e.isDeleted = false
              AND (:excludeId IS NULL OR e.id <> :excludeId)
            """)
    boolean existsByCnssMatriculeAndIdNotAndIsDeletedFalse(
            @Param("cnssMatricule") String cnssMatricule,
            @Param("excludeId") UUID excludeId
    );
}
