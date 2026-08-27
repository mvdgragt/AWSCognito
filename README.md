# Uppgiftshanteraren: AWS Cognito-projekt

En webbapplikation där användare kan registrera konto, logga in och hantera personliga uppgifter (att-göra-listor). Byggd med React (frontend) och Spring Boot (backend) med fullständig **AWS Cognito-integration på serversidan**.

**Live:** _https://spring-boot-backend.d37tud3z6getmf.amplifyapp.com/_

---

## Hur applikationen fungerar

Applikationen består av:
- **Frontend**: React-baserat gränssnitt (Vite)
- **Backend**: Spring Boot-applikation med direktintegrerad AWS Cognito-kontroll

### Funktioner

- **Registrering**: Användaren skickar e-post och lösenord till Spring Boot backend (`POST /auth/signup`). Backend kommunicerar direkt med AWS Cognito User Pool och registrerar användaren. En verifieringskod skickas till e-postadressen.

- **Bekräftelse**: Användaren anger verifieringskoden (`POST /auth/confirm`). Backend validerar koden mot Cognito.

- **Inloggning**: Inloggning sker mot Spring Boot backend (`POST /auth/login`). Backend autentiserar användaren mot Cognito User Pool och returnerar JWT-tokens (access token, ID token, refresh token). Tokens lagras i frontendens `localStorage`.

- **Uppgifter**: Inloggad användare kan lägga till, hämta och ta bort egna uppgifter via `/todos`. Alla förfrågningar inkluderar JWT-tokens i `Authorization`-headern. Spring Boot validerar tokens och läser användarens ID direkt från JWT-claimet (`sub`), så en användare kan aldrig komma åt någon annans uppgifter.

- **Utloggning**: Sessionen avslutas genom att tokens raderas från `localStorage` på klientsidan.

- **Ta bort konto**: Inloggad användare kan radera sitt konto och all tillhörande data (`DELETE /account`). Backend raderar först användarens uppgifter från DynamoDB, sedan användarens Cognito-konto via AWS SDK.

---

## Backend: Spring Boot med Cognito- och DynamoDB-integration

Backenden är en **Spring Boot-applikation** (Java 21) med direktintegrerad **AWS Cognito User Pool**-kontroll och **DynamoDB**-datalagring. Källkoden finns i mappen [`backend/src/main/java/com/example/cognitobackend/`](backend/src/main/java/com/example/cognitobackend/).

### Arkitektur

**Autentisering (`AuthController`)**:
- `POST /auth/signup` – Registrerar nya användare direkt i Cognito User Pool
- `POST /auth/confirm` – Bekräftar användarens e-postadress med verifieringskod
- `POST /auth/login` – Autentiserar användare mot Cognito och returnerar JWT-tokens
- `GET /auth/me` – Returnerar info om inloggad användare (från JWT-claims)

**Uppgifter (`TodoController`)**:
- `GET /todos` – Hämtar alla uppgifter för den inloggade användaren
- `POST /todos` – Skapar en ny uppgift
- `DELETE /todos/{todoId}` – Raderar en uppgift

**Konto (`AccountController`)**:
- `DELETE /account` – Raderar användarens uppgifter från DynamoDB och användarens Cognito-konto

**Säkerhet (`SecurityConfig`)**:
- Alla endpoints (förutom `/auth/signup`, `/auth/confirm`, `/auth/login`) kräver giltigt JWT-token från Cognito
- JWT-tokens valideras automatiskt av **Spring Security OAuth2 Resource Server**
- Tokens utfärdas av AWS Cognito User Pool (`eu-north-1`)
- CORS är konfigurerat för att tillåta anrop från frontendens origin

**AWS SDK-integration**:
- `CognitoIdentityProviderClient` (`CognitoClientConfig`) och `DynamoDbEnhancedClient`/`DynamoDbTable<Todo>` (`DynamoDbConfig`) konfigureras som Spring-beans
- Autentisering mot Cognito görs via `CognitoService`
- Läsning/skrivning av uppgifter görs via `TodoRepository`
- Ingen extern API Gateway JWT-auktoriserare – allt hanteras i kod

Projektet byggs med **Maven** (`backend/pom.xml`).

---

## Koppling till AWS

### AWS Cognito User Pool (direktintegrerad i Spring Boot)

Autentiseringen hanteras av **AWS Cognito User Pool** (`eu-north-1`). Spring Boot-backenden kommunicerar direkt med Cognito via AWS SDK.

**Operationer som Spring Boot-koden gör**:
- `SignUpRequest` – Registrerar nya användare
- `ConfirmSignUpRequest` – Bekräftar användarens e-post
- `InitiateAuthRequest` – Autentiserar användare och hämtar JWT-tokens
- `DeleteUserRequest` – Raderar användarkonto

**JWT-validering**: Spring Boot validerar JWT-tokens från Cognito automatiskt via OAuth2 Resource Server. Tokens innehåller `sub` (user ID) och `email`, som används för att identifiera användaren i skyddade endpoints.

### Frontend-kommunikation

Frontendens React-app kommunicerar med Spring Boot-backenden via `fetch`, inte via `aws-amplify`:
- Registrering, bekräftelse och inloggning sker via `/auth/*`-endpoints
- JWT-tokens från login-svaret lagras i `localStorage`
- Alla skyddade API-anrop inkluderar access token i `Authorization`-headern som Bearer-token

Frontenden har **ingen direktkoppling till Cognito** – all auth-logik går genom Spring Boot-backend.

### AWS DynamoDB (datalagring)

Uppgifterna lagras i en DynamoDB-tabell (`todos`) med `userId` (partition key) och `todoId` (sort key). Spring Boot läser `userId` från JWT-tokens (claim `sub`) och säkerställer att varje användare bara kan läsa/skriva sina egna poster.

---

## Ta bort konto och personlig data

En inloggad användare kan ta bort sitt konto och all tillhörande data via knappen **"Ta bort mitt konto"** på dashboardsidan.

När användaren bekräftar raderingen sker följande i ordning:

1. Frontend skickar `DELETE`-förfrågan till Spring Boot backend (`DELETE /account`)
2. Backend hämtar alla uppgifter kopplade till användaren från DynamoDB och raderar dem
3. Backend raderar användarkontot från AWS Cognito via `DeleteUserRequest` (AWS SDK)
4. Frontend rensar sina lagrade tokens och sessionen avslutas
5. Användaren omdirigeras till inloggningssidan

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
| Databas | AWS DynamoDB (via `dynamodb-enhanced` SDK) |
| Region | eu-north-1 (Stockholm) |

---

## Köra applikationen lokalt

### Starta Spring Boot-backend

```bash
cd backend

export COGNITO_USER_POOL_ID=<din-user-pool-id>
export COGNITO_CLIENT_ID=<din-app-client-id>

mvn spring-boot:run
```

Backend startar på `http://localhost:8080` och exponerar endpoints:
- `POST /auth/signup` – Registrering
- `POST /auth/confirm` – Bekräftelse
- `POST /auth/login` – Inloggning
- `GET /auth/me` – Aktuell användare (kräver JWT-token)
- `GET/POST /todos`, `DELETE /todos/{todoId}` – Uppgifter (kräver JWT-token)
- `DELETE /account` – Ta bort konto (kräver JWT-token)

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

### Spring Boot-konfiguration

Spring Boot kräver följande miljövariabler för Cognito-tillgång:
COGNITO_USER_POOL_ID=<din-user-pool-id>
COGNITO_CLIENT_ID=<din-app-client-id>