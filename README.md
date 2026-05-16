# Uppgiftshanteraren – AWS Cognito-projekt

En webbapplikation där användare kan registrera konto, logga in och hantera personliga uppgifter (att-göra-listor). Byggd med React och kopplad till AWS-tjänster för autentisering, API och datalagring.

**Live:** https://main.d3kcx9eqx8e1al.amplifyapp.com

---

## Hur applikationen fungerar

Applikationen består av ett React-baserat gränssnitt och ett serverlöst backend byggt på AWS.

### Funktioner

- **Registrering** – Användaren skapar ett konto med e-post och lösenord. En verifieringskod skickas till e-postadressen för att bekräfta kontot.
- **Inloggning** – Inloggning sker via e-post och lösenord. En giltig Cognito-session skapas och lagras lokalt.
- **Uppgifter** – Inloggad användare kan lägga till och ta bort egna uppgifter. Uppgifterna är kopplade till användarens konto och visas enbart för den inloggade användaren.
- **Utloggning** – Sessionen avslutas och användaren skickas tillbaka till inloggningssidan.
- **Ta bort konto** – Inloggad användare kan radera sitt konto och all tillhörande data permanent (se nedan).

---

## Koppling till AWS och Cognito

### AWS Cognito

Autentiseringen hanteras av **AWS Cognito User Pool** (`eu-north-1`). AWS Amplify används på klientsidan för att kommunicera med Cognito.

- `signUp` – Registrerar en ny användare i User Pool
- `confirmSignUp` – Bekräftar kontot med verifieringskoden
- `signIn` – Loggar in och hämtar JWT-tokens (access token + ID token)
- `signOut` – Avslutar sessionen lokalt
- `fetchAuthSession` – Hämtar aktuell session och access token som används i API-anrop

### AWS API Gateway + Lambda

API-anrop går via en **AWS HTTP API** (API Gateway) som skyddas av en **JWT-auktoriserare** kopplad till Cognito User Pool. Det innebär att alla anrop måste inkludera ett giltigt Cognito access token i `Authorization`-headern.

Bakom API Gateway finns en **AWS Lambda-funktion** (Node.js) som hanterar all logik:

| Metod | Sökväg | Beskrivning |
|-------|--------|-------------|
| GET | `/todos` | Hämtar alla uppgifter för inloggad användare |
| POST | `/todos` | Skapar en ny uppgift |
| DELETE | `/todos` | Tar bort en specifik uppgift |
| DELETE | `/account` | Tar bort kontot och all data (se nedan) |

### AWS DynamoDB

Uppgifterna lagras i en **DynamoDB-tabell** (`todos`) med `userId` (partition key) och `todoId` (sort key). Varje användare kan enbart läsa och skriva sina egna poster tack vare att `userId` hämtas från JWT-tokens claims i Lambda.

---

## Ta bort konto och personlig data

En inloggad användare kan ta bort sitt konto och all tillhörande data via knappen **"Ta bort mitt konto"** på dashboardsidan.

När användaren bekräftar raderingen sker följande i ordning:

1. Lambda hämtar alla uppgifter kopplade till användaren i DynamoDB
2. Varje uppgift raderas från databasen
3. Användarkontot raderas från AWS Cognito via `DeleteUser`-API:et med användarens access token
4. Sessionen avslutas på klientsidan via `signOut`
5. Användaren omdirigeras till inloggningssidan

Efter detta kan användaren inte längre logga in med sina uppgifter och all data är permanent borttagen.

---

## Teknisk stack

| Del | Teknologi |
|-----|-----------|
| Frontend | React 19, Vite, Tailwind CSS v4 |
| Routing | TanStack Router |
| Datahantering | TanStack Query |
| Autentisering (klient) | AWS Amplify JS v6 |
| Autentisering (server) | AWS Cognito User Pool |
| API | AWS API Gateway (HTTP API) |
| Backend | AWS Lambda (Node.js, ES Modules) |
| Databas | AWS DynamoDB |
| Region | eu-north-1 (Stockholm) |

---

## Köra applikationen lokalt

```bash
# Installera beroenden
pnpm install

# Starta utvecklingsservern
pnpm dev
```

Applikationen kräver en `.env`-fil med följande variabler:

```
VITE_COGNITO_USER_POOL_ID=<user-pool-id>
VITE_COGNITO_CLIENT_ID=<app-client-id>
VITE_COGNITO_REGION=eu-north-1
VITE_API_URL=/api/todos
```
