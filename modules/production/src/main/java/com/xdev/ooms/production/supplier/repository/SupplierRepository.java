package com.xdev.ooms.production.supplier.repository;



import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;

public interface SupplierRepository extends BaseRepository<Supplier> {
    java.util.Optional<Supplier> findFirstByMatriculeFiscalIgnoreCaseAndIsDeletedFalse(String matriculeFiscal);

    java.util.Optional<Supplier> findFirstByPhoneAndNameIgnoreCaseAndLastnameIgnoreCaseAndIsDeletedFalse(
            String phone, String name, String lastname);
}