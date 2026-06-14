package com.xdev.ooms.production.waste.repository;



import com.xdev.ooms.production.waste.entity.Waste;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WasteRepository extends BaseRepository<Waste> {
}
