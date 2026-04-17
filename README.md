# menteaberta-srv-auth

Serviço de autenticação da plataforma **Mente Aberta** — conecta psicólogos voluntários a pacientes anônimos.

Responsável por cadastro, login, emissão e validação de tokens JWT. Futuramente será consumido pelo `menteaberta-srv-gateway`.

---

## Tecnologias

- Java 17
- Spring Boot 3.5
- Spring Security + JWT (JJWT 0.12)
- Spring Data JPA + PostgreSQL
- Lombok
- SpringDoc OpenAPI (Swagger)
- Docker + Nginx

---

## Endpoints

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/api/auth/register` | Público | Cadastra novo usuário |
| `POST` | `/api/auth/login` | Público | Autentica e retorna tokens JWT |
| `POST` | `/api/auth/refresh` | Público | Renova o access token |
| `GET` | `/api/auth/validate` | Autenticado | Valida token e retorna role (usado pelo gateway) |

### Exemplos

**Register**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "paciente@email.com",
  "senha": "Senha@123",
  "role": "PACIENTE"
}
```

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "paciente@email.com",
  "senha": "Senha@123"
}
```

**Refresh**
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "<refresh_token>"
}
```

**Validate**
```http
GET /api/auth/validate
Authorization: Bearer <access_token>
```

Resposta:
```json
{ "role": "PACIENTE" }
```

---

## Roles

| Role | Descrição |
|---|---|
| `PACIENTE` | Acesso à área de pacientes |
| `PSICOLOGO` | Acesso à área de psicólogos |

---

## Banco de Dados

Utiliza PostgreSQL com schema compartilhado `menteaberta` — o mesmo schema usado por todos os microsserviços da plataforma.

**Tabela:** `menteaberta.usuario`

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Chave primária, gerado automaticamente |
| `email` | VARCHAR | Único, não nulo |
| `senha` | VARCHAR | Hash BCrypt |
| `role` | VARCHAR | `PACIENTE` ou `PSICOLOGO` |
| `ativo` | BOOLEAN | Default `true` |
| `criado_em` | TIMESTAMP | Preenchido automaticamente |

O schema é criado automaticamente via `schema.sql` na inicialização. As tabelas são gerenciadas pelo Hibernate (`ddl-auto: update`).

---

## Variáveis de Ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_URL` | Sim | JDBC URL do PostgreSQL |
| `DB_USERNAME` | Sim | Usuário do banco |
| `DB_PASSWORD` | Sim | Senha do banco |
| `JWT_SECRET` | Sim | Chave Base64 para assinar tokens (mín. 32 bytes) |
| `SERVER_PORT` | Não | Porta da aplicação (default: `8081`) |
| `JWT_EXPIRATION_MS` | Não | Expiração do access token (default: `86400000` — 24h) |
| `JWT_REFRESH_EXPIRATION_MS` | Não | Expiração do refresh token (default: `604800000` — 7 dias) |

Para gerar uma chave JWT segura:
```bash
openssl rand -base64 64
```

---

## Rodando Localmente

### Pré-requisitos
- Java 17+
- Maven ou usar o `./mvnw` incluso
- PostgreSQL acessível

### Configurar variáveis
Crie um arquivo `.env` ou exporte as variáveis:
```env
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=sua-senha
JWT_SECRET=sua-chave-base64
```

### Build e execução
```bash
./mvnw clean package -DskipTests
java -jar target/auth-0.0.1-SNAPSHOT.jar
```

### Testes
```bash
./mvnw test
```

Os testes de integração usam H2 em memória — nenhuma conexão externa necessária.

---

## Docker

### Build da imagem
```bash
docker build -t menteaberta-srv-auth .
```

### Executar o container
```bash
docker run -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://host:5432/postgres \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=senha \
  -e JWT_SECRET=chave-base64 \
  menteaberta-srv-auth
```

O `nginx.conf` incluso configura um proxy reverso na porta `2001` apontando para o serviço na porta `8081`.

---

## Documentação da API

Com a aplicação rodando, acesse:

```
http://localhost:8081/swagger-ui.html
```

---

## Estrutura do Projeto

```
src/main/java/site/menteaberta/auth/
├── AuthApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   └── AuthController.java
├── dto/
│   ├── AuthResponse.java
│   ├── ErrorResponse.java
│   ├── LoginRequest.java
│   └── RegisterRequest.java
├── exception/
│   ├── EmailJaCadastradoException.java
│   └── GlobalExceptionHandler.java
├── model/
│   ├── RoleUsuario.java
│   └── Usuario.java
├── repository/
│   └── UsuarioRepository.java
├── security/
│   ├── JwtAuthFilter.java
│   ├── JwtService.java
│   └── UserDetailsServiceImpl.java
└── service/
    └── AuthService.java
```
