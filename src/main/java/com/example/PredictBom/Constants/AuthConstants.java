package com.example.PredictBom.Constants;

public class AuthConstants {
    public final static int PASSWORDS_NOT_EQUALS = 1;
    public final static int INVALID_TOKEN = 2;
    public final static int EXPIRED_TOKEN = 3;
    public final static int CORRECT_TOKEN = 4;
    public final static int UPDATED_PASSWORD = 5;

    public static final String USERNAME_ALREADY_USED_INFO = "Nazwa użytkownika jest już zajęta";
    public static final String EMAIL_ALREADY_USED_INFO = "Podany adres e-mail jest już zajęty";
    public static final String USER_SUCCESSFUL_REGISTERED = "Użytkownik został poprawnie zarejestrowany";
    public static final String ROLE_IS_NOT_FOUND_INFO = "Nie znaleziono podanej roli użytkownika";
    public static final String PASSWORD_CHANGED_INFO = "Hasło zostało zmienione. Zaloguj się przy pomocy nowego hasła !";
    public static final String INCORRECT_PASSWORD_INFO = "Nie udało się zmienić hasła - niepoprawne hasło.";
    public static final String PASSWORD_USER_NOT_FOUND_INFO = "Nie udało się zmienić hasła- nie znaleziono użytkownika.";
    public static final String DIFFERENT_PASSWORDS_INFO ="Nie udało się zmienić hasła - hasła nie są identyczne.";
    public static final String USER_NOT_FOUND_BY_MAIL_INFO = "Nie znaleziono żadnego użytkownika o tym adresie e-mail";
}
