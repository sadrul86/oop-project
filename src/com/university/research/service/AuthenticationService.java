package com.university.research.service;

import com.university.research.model.Researcher;
import com.university.research.model.UserAccount;
import com.university.research.repository.ResearchRepository;
import com.university.research.util.PasswordUtil;

import java.util.List;
import java.util.Locale;

public class AuthenticationService {
    private final ResearchRepository repository;

    public AuthenticationService(ResearchRepository repository) {
        this.repository = repository;
    }

    public UserAccount authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        UserAccount account = repository.findUserAccountByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (!account.isActive() || !PasswordUtil.matches(password, account.getPasswordSalt(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return account;
    }

    public UserAccount register(String name, String email, String password, String department, String role) {
        require(name, "Name");
        require(email, "Email");
        require(password, "Password");
        require(department, "Department");
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.contains("@")) throw new IllegalArgumentException("Enter a valid email address.");
        if (password.length() < 6) throw new IllegalArgumentException("Password must contain at least 6 characters.");
        if (repository.findUserAccountByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        boolean profileEmailExists = repository.findAllResearchers().stream()
                .anyMatch(r -> r.getEmail().equalsIgnoreCase(normalizedEmail));
        if (profileEmailExists) {
            throw new IllegalArgumentException("A research profile with this email already exists. Use its linked account instead.");
        }

        UserAccount.Role accountRole;
        try {
            accountRole = UserAccount.Role.valueOf(role == null ? "STUDENT" : role.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("Account role must be STUDENT or FACULTY.");
        }
        if (accountRole == UserAccount.Role.ADMIN) throw new IllegalArgumentException("Admin accounts cannot be self-registered.");

        Researcher.Role researcherRole = accountRole == UserAccount.Role.FACULTY
                ? Researcher.Role.FACULTY : Researcher.Role.STUDENT;
        Researcher researcher = new Researcher(
                repository.nextResearcherId(), name.trim(), normalizedEmail, department.trim(), researcherRole,
                List.of(), List.of(), "", true);
        repository.saveResearcher(researcher);

        String salt = PasswordUtil.newSalt();
        UserAccount account = new UserAccount(
                repository.nextUserAccountId(), normalizedEmail, name.trim(), accountRole,
                salt, PasswordUtil.hash(password, salt), researcher.getId(), true);
        return repository.saveUserAccount(account);
    }

    public UserAccount getUser(int userId) {
        return repository.findUserAccountById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User account not found."));
    }

    private void require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required.");
    }
}
