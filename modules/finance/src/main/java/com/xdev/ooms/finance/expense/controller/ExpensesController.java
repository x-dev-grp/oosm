package com.xdev.ooms.finance.expense.controller;


import com.xdev.ooms.finance.expense.dto.ExpenseDto;
import com.xdev.ooms.finance.expense.entity.Expense;
 import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/expense")

public class ExpensesController extends BaseControllerImpl<Expense, ExpenseDto, ExpenseDto>  {


    public ExpensesController(BaseService<Expense, ExpenseDto, ExpenseDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "Expense".toUpperCase();
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
