package com.ratibu.account.api;

import com.ratibu.account.application.CreateAccountCommand;
import com.ratibu.account.application.CreateAccountResult;
import com.ratibu.account.application.CreateAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final CreateAccountService createAccountService;

    public AccountController(CreateAccountService createAccountService) {
        this.createAccountService = createAccountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        CreateAccountCommand command = new CreateAccountCommand(
                Optional.ofNullable(request.accountId()).filter(id -> !id.isBlank()),
                request.email(),
                Optional.ofNullable(request.displayName()));

        CreateAccountResult result = createAccountService.execute(command);
        AccountResponse body = AccountResponse.from(result);

        HttpStatus status = result.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
