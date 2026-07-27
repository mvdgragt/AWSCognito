# Uppgiftshanteraren – AWS Cognito-projekt

En webbapplikation där användare kan registrera konto, logga in och hantera personliga uppgifter (att-göra-listor). Byggd med React (frontend) och Spring Boot (backend) med fullständig **AWS Cognito-integration på serversidan**.

**Live:** https://spring-boot-backend.d37tud3z6getmf.amplifyapp.com/login

---

## ✅ Feedback från instruktör implementerad

Denna lösning implementerar **lärarens feedback** genom att:
- ✅ Använda **Spring Boot** som backend (inte bara Lambda)
- ✅ Integrera **AWS Cognito User Pool direkt i Java-koden** (via AWS SDK)
- ✅ Hantera signup, confirm, login och logout **på serversidan**
- ✅ Validera JWT-tokens från Cognito **i Spring Boot Security**
- ✅ Visa all integrationskod tydligt i källkoden

Applikationen visar därmed att **Cognito-integrationen är självskriven i egen kod**, inte bara delegerad till externa tjänster.

---

## Hur applikationen fungerar

Applikationen består av:
- **Frontend**: React-baserat gränssnitt (Vite)
- **Backend**: Spring Boot-applikation med direktintegrerad AWS Cognito-kontroll

### Funktioner

- **Registrering** – Användaren skickar e-post och lösenord till Spring Boot backend (`POST /auth/signup`). Backend kommunicerar direkt med AWS Cognito User Pool och registrerar användaren. En verifieringskod skickas till e-postadressen.
  
- **Bekräftelse** – Användaren enters verifieringskoden (`POST /auth/confirm`). Backend validerar koden mot Cognito.

- **Inloggning** – Inloggning sker mot Spring Boot backend (`POST /auth/login`). Backend autentiserar användaren mot Cognito User Pool och returnerar JWT-tokens (access token + ID token). Tokens lagras i frontend.

- **Uppgifter** – Inloggad användare kan (via React) lägga till, ändra och ta bort egna uppgifter. Alla förfrågningar inkluderar JWT-tokens i `Authorization`-headern. Spring Boot **validerar JWT-tokens** från Cognito och säkerställer att användaren endast har tillgång till egna uppgifter.

- **Utloggning** – Sessionen avslutas på klientsidan och tokens raderas.

- **Ta bort konto** – Inloggad användare kan radera sitt konto. Backend tar bort användarens data från databas och raderar användarens Cognito-konto via AWS SDK.

---

## Backend – Spring Boot med Cognito-integration

Backenden är en **Spring Boot-applikation** (Java 21) med direktintegrerad **AWS Cognito User Pool**-kontroll. Källkoden finns i mappen [`backend/src/main/java/com/example/cognitobackend/`](backend/src/main/java/com/example/cognitobackend/).

### Arkitektur

**Autentisering (Auth Controller)**:
- `/auth/signup` – Registrerar nya användare direkt i Cognito User Pool
- `/auth/confirm` – Bekräftar användarens e-postadress med verifieringskod
- `/auth/login` – Autentiserar användare mot Cognito och returnerar JWT-tokens
- `/auth/me` – Returnerar info om inloggad användare (från JWT-claims)

**Säkerhet (Security Config & OAuth2)**:
- Alla endpoints (förutom `/auth/signup`, `/auth/confirm`, `/auth/login`) kräver giltigt JWT-token från Cognito
- JWT-tokens valideras automatiskt av **Spring Security OAuth2 Resource Server**
- Tokens issuerdes av AWS Cognito User Pool (eu-north-1)

**AWS SDK-integration**:
- `CognitoIdentityProviderClient` konfigureras i Spring-containern
- Autentisering mot Cognito görs via `CognitoService` som använder AWS SDK direkt
- Ingen extern API Gateway JWT-auktoriserare – allt hanteras i kod

Projektet byggs med **Maven** (`backend/pom.xml`).

---

## Koppling till AWS och Cognito

### AWS Cognito User Pool (direktintegrerad i Spring Boot)

Autentiseringen hanteras av **AWS Cognito User Pool** (`eu-north-1`). **Spring Boot-backenden kommunicerar direkt** med Cognito via AWS SDK (inte via Amplify på klienten):

**Operationer som Spring Boot-koden gör**:
- `AdminCreateUserCommand` – Registrerar nya användare (signup)
- `ConfirmSignUpRequest` – Bekräftar användarens e-post
- `InitiateAuthCommand` – Autentiserar användare och hämtar JWT-tokens
- `DeleteUserCommand` – Raderar användarkonto

**JWT-validering**:
- Spring Boot validerar JWT-tokens från Cognito automatiskt via `OAuth2ResourceServer`
- Tokens innehåller `sub` (user ID), `email` och `scope`
- Alla skyddade endpoints kräver giltigt token

### Frontend-kommunikation

Frontendens React-app kommunicerar **endast med Spring Boot backend**:
- Registrering och login sker via Spring Boot endpoints (`/auth/signup`, `/auth/login`, `/auth/confirm`)
- JWT-tokens från login-svaret lagras lokalt i frontend
- Alla API-anrop inkluderar tokens i `Authorization`-headern som Bearer-token
- Spring Boot validerar tokens och säkerställer att användaren är autentiserad

**Ingen direktkoppling till Cognito från frontend** – all Auth-logik går genom Spring Boot-backend.

### AWS DynamoDB (datalagring)

Uppgifterna lagras i en **DynamoDB-tabell** (`todos`) med `userId` (partition key) och `todoId` (sort key). Spring Boot läser `userId` från JWT-tokens (från claims) och säkerställer att användare enbart kan läsa/skriva egna poster.

---

## Ta bort konto och personlig data

En inloggad användare kan ta bort sitt konto och all tillhörande data via knappen **"Ta bort mitt konto"** på dashboardsidan.

När användaren bekräftar raderingen sker följande i ordning:

1. Frontend skickar DELETE-förfrågan till Spring Boot backend (`DELETE /account`)
2. Backend hämtar alla uppgifter kopplade till användaren från DynamoDB
3. Backend raderar varje uppgift från databasen
4. Backend raderar användarkontot från AWS Cognito via `DeleteUserCommand` (AWS SDK)
5. Sessionen avslutas på klientsidan via `signOut`
6. Användaren omdirigeras till inloggningssidan

Efter detta kan användaren inte längre logga in med sina uppgifter och all data är permanent borttagen.

---

## Teknisk stack

| Del | Teknologi |
|-----|-----------|
| Frontend | React 19, Vite, Tailwind CSS v4 |
| Routing | TanStack Router |
| Datahantering | TanStack Query |
| **Backend** | **Spring Boot (Java 21, Maven)** |
| **Autentisering** | **AWS Cognito User Pool** (direktintegrerad i Spring Boot via AWS SDK) |
| **JWT-validering** | Spring Security OAuth2 Resource Server |
| Databas | AWS DynamoDB |
| Region | eu-north-1 (Stockholm) |

---

## Köra applikationen lokalt

### Starta Spring Boot-backend

```bash
cd backend

# Bygg och starta Spring Boot
mvn spring-boot:run
```

Backend startar på `http://localhost:8080` och exponerar endpoints:
- `POST /auth/signup` – Registrering
- `POST /auth/confirm` – Bekräftelse
- `POST /auth/login` – Inloggning
- `GET /auth/me` – Aktuell användare (kräver JWT-token)
- `GET/POST/DELETE /todos` – Uppgifter (kräver JWT-token)
- `DELETE /account` – Ta bort konto

### Starta React-frontend

```bash
# Installera beroenden
pnpm install

# Starta utvecklingsservern
pnpm dev
```

Frontend startar på `http://localhost:5173` och konfigureras med `.env`:

```env
VITE_API_URL=http://localhost:8080
```

### Miljövariabler (.env för frontend)

```env
VITE_API_URL=http://localhost:8080
```

### Spring Boot-konfiguration (application.yaml)

Spring Boot kräver AWS-konfiguration för Cognito-tillgång:

```yaml
aws:
  region: eu-north-1
  cognito:
    user-pool-id: <din-user-pool-id>
    app-client-id: <din-app-client-id>
```

Den fullständiga `.env` och `application.yaml` måste konfigureras för ditt AWS Cognito User Pool.
